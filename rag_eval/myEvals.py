import os
import sys
import requests
import json
from pathlib import Path
import logging
import pandas as pd
import asyncio
from datetime import datetime

from openai import OpenAI
from ragas import EvaluationDataset, evaluate, SingleTurnSample
from ragas.llms import llm_factory
from ragas.metrics import (
    ContextPrecision,
    ContextRecall,
    Faithfulness,
    AnswerRelevancy
)
from langchain_community.embeddings import ZhipuAIEmbeddings
from langchain_core.embeddings import Embeddings
from typing import List, Dict, Any
sys.path.insert(0, str(Path(__file__).parent))

# 初始化日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('eval.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)

class ZhipuAIEmbeddingsWrapper(Embeddings):
    """包装 ZhipuAIEmbeddings，添加 embed_query 方法"""

    def __init__(self, zhipu_embeddings: ZhipuAIEmbeddings):
        self.zhipu_embeddings = zhipu_embeddings

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        return self.zhipu_embeddings.embed_documents(texts)

    def embed_query(self, text: str) -> List[float]:
        if hasattr(self.zhipu_embeddings, 'embed_query'):
            return self.zhipu_embeddings.embed_query(text)
        return self.zhipu_embeddings.embed_documents([text])[0]


# 创建 embeddings 实例
embeddings = ZhipuAIEmbeddingsWrapper(
    ZhipuAIEmbeddings(
        model="embedding-2",

        base_url="https://open.bigmodel.cn/api/paas/v4/embeddings"
    )
)

deepseek_client = OpenAI(

    base_url="https://api.deepseek.com/v1"
)
llm = llm_factory("deepseek-chat", provider="openai", client=deepseek_client)


def remove_tags(text):
    """移除文本中的 HTML 标签"""
    import re
    if text is None:
        return ""
    return re.sub(r'<[^>]+>', '', text)


def query_ragas_api(question: str) -> dict:
    """
    调用 RAGAS API 获取回答和检索上下文
    """
    url = "http://localhost:8191/chat/ragas/query"
    payload = {
        "query": question,
        "hyde": False
    }
    headers = {
        "Content-Type": "application/json"
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=90)
        response.raise_for_status()

        result = response.json()

        if result.get("success") and result.get("data"):
            return {
                "answer": result["data"].get("answer", ""),
                "contexts": result["data"].get("contexts", [])
            }
        else:
            logging.error(f"API 返回错误: {result.get('message')}")
            return {"answer": "", "contexts": []}

    except requests.exceptions.RequestException as e:
        logging.error(f"请求 API 失败: {e}")
        return {"answer": "", "contexts": []}
    except json.JSONDecodeError as e:
        logging.error(f"解析 JSON 失败: {e}")
        return {"answer": "", "contexts": []}


def load_excel_data(excel_path: str) -> pd.DataFrame:
    """加载 Excel 文件"""
    try:
        df = pd.read_excel(excel_path)
        logging.info(f"成功加载 Excel 文件，共 {len(df)} 行数据")
        logging.info(f"Excel 列名: {df.columns.tolist()}")
        return df
    except Exception as e:
        logging.error(f"加载 Excel 文件失败: {e}")
        raise


def save_excel_data(df: pd.DataFrame, excel_path: str):
    """保存 Excel 文件"""
    try:
        df.to_excel(excel_path, index=False)
        logging.info(f"成功保存 Excel 文件: {excel_path}")
    except Exception as e:
        logging.error(f"保存 Excel 文件失败: {e}")
        raise


async def evaluate_single_sample(sample: SingleTurnSample, metrics: List) -> Dict[str, Any]:
    """
    评估单个样本
    """
    from ragas import evaluate

    try:
        # 为单个样本创建评估数据集
        eval_dataset = EvaluationDataset(samples=[sample])
        # 执行评估
        result = evaluate(dataset=eval_dataset, metrics=metrics, llm=llm)
        # 提取各项指标分数 - ragas 新版本返回 EvaluationResult
        scores = {}
        
        # 方法1: 尝试使用 scores 属性
        if hasattr(result, 'scores'):
            scores_data = result.scores
            # scores 可能是 DataFrame, list 或 dict
            if hasattr(scores_data, 'to_dict'):  # DataFrame
                scores = scores_data.to_dict('records')[0] if len(scores_data) > 0 else {}
            elif isinstance(scores_data, list) and len(scores_data) > 0:
                scores = scores_data[0] if isinstance(scores_data[0], dict) else {}
            elif isinstance(scores_data, dict):
                scores = scores_data
        
        # 方法2: 尝试使用 df 属性
        if not scores and hasattr(result, 'df') and result.df is not None and not result.df.empty:
            row = result.df.iloc[0]
            for col in result.df.columns:
                scores[col] = row[col]
        
        # 方法3: 尝试直接从 result 提取（作为属性访问）
        if not scores:
            for metric in metrics:
                metric_name = metric.__class__.__name__.lower()
                if hasattr(result, metric_name):
                    scores[metric_name] = getattr(result, metric_name)

        return scores
    except Exception as e:
        logging.error(f"评估单个样本失败: {e}")
        import traceback
        logging.error(traceback.format_exc())
        return {}


async def main():
    # 文件路径
    excel_path = Path(__file__).parent / "rag评测数据集.xlsx"
    output_path = Path(__file__).parent / "rag评测数据集_结果.xlsx"

    # 加载数据
    logging.info(f"正在加载 Excel 文件: {excel_path}")
    df = load_excel_data(str(excel_path))

    # 假设 Excel 列名为: 编号, 问题, 期望回答
    # 根据实际情况调整列名
    question_col = "question"
    reference_col = "expected answer"
    id_col = "编号"

    # 确保必要的列存在
    required_cols = [id_col, question_col, reference_col]
    for col in required_cols:
        if col not in df.columns:
            logging.error(f"Excel 缺少必要列: {col}")
            logging.error(f"可用列: {df.columns.tolist()}")
            return

    # 添加结果列（如果不存在）- 指定正确的类型
    result_cols = {
        "answer": object,
        "contexts": object,
        "context_precision": float,
        "context_recall": float,
        "faithfulness": float,
        "answer_relevancy": float
    }
    for col, dtype in result_cols.items():
        if col not in df.columns:
            df[col] = pd.Series([None] * len(df), dtype=dtype)
        else:
            # 转换现有列为正确类型
            df[col] = df[col].astype(dtype)

    # 创建评估指标
    metrics = [
        ContextPrecision(),
        ContextRecall(),
        Faithfulness(),
        AnswerRelevancy(embeddings=embeddings),
    ]

    # ============ 控制处理范围 ============
    MAX_SAMPLES = None   # 设置为 None 表示读取全部，设置为 1 表示只读取 1 条测试
    START_FROM = 1      # 从第几条开始处理（1=从第1条开始，32=从第32条开始）
    END_AT = 100        # 处理到第几条结束（None=处理到全部，50=处理到第50条）
    # =====================================

    # 限制处理范围
    if START_FROM > 1:
        df = df.iloc[START_FROM - 1:]  # 跳过前面的数据
        logging.info(f"从第 {START_FROM} 条开始处理")
    if END_AT is not None:
        df = df.head(END_AT - START_FROM + 1)
        logging.info(f"处理到第 {END_AT} 条结束")
    if MAX_SAMPLES is not None:
        df = df.head(MAX_SAMPLES)
        logging.info(f"测试模式: 仅处理前 {MAX_SAMPLES} 条数据")

    total = len(df)
    success_count = 0
    fail_count = 0

    logging.info(f"开始处理 {total} 个问题（原始编号: {START_FROM} ~ {START_FROM + total - 1}）...")

    # 循环计数器，用于计算原始编号
    for idx, row in df.iterrows():
        question = row[question_col]
        reference = row[reference_col]
        # 计算原始编号：START_FROM + 当前循环位置
        current_num = START_FROM + (df.index.get_loc(idx) if hasattr(df.index, 'get_loc') else list(df.index).index(idx))
        question_id = row.get(id_col, current_num)

        logging.info(f"[{current_num}/{START_FROM + total - 1}] 正在处理问题: {question[:50]}...")

        # 调用 API 获取回答和上下文
        api_response = query_ragas_api(question)
        answer = api_response.get("answer", "")
        contexts = api_response.get("contexts", [])

        if not answer:
            logging.warning(f"问题 [{question_id}] 未获取到回答，跳过")
            fail_count += 1
            continue

        # 清理 answer 中的 HTML 标签
        clean_answer = remove_tags(answer)

        # 创建样本
        sample = SingleTurnSample(
            user_input=question,
            retrieved_contexts=contexts,
            response=clean_answer,
            reference=reference
        )

        # 执行评估
        try:
            scores = await evaluate_single_sample(sample, metrics)

            # 更新 DataFrame
            df.at[idx, "answer"] = answer
            df.at[idx, "contexts"] = json.dumps(contexts, ensure_ascii=False)

            # 填充各项指标
            if "context_precision" in scores:
                df.at[idx, "context_precision"] = scores["context_precision"]
            if "context_recall" in scores:
                df.at[idx, "context_recall"] = scores["context_recall"]
            if "faithfulness" in scores:
                df.at[idx, "faithfulness"] = scores["faithfulness"]
            if "answer_relevancy" in scores:
                df.at[idx, "answer_relevancy"] = scores["answer_relevancy"]

            # 格式化分数输出
            def fmt_score(score):
                if score is None or (isinstance(score, float) and pd.isna(score)):
                    return "N/A"
                if isinstance(score, (int, float)):
                    return f"{score:.4f}"
                return str(score)

            success_count += 1
            logging.info(f"问题 [{question_id}] 评估完成: "
                        f"context_precision={fmt_score(scores.get('context_precision'))}, "
                        f"context_recall={fmt_score(scores.get('context_recall'))}, "
                        f"faithfulness={fmt_score(scores.get('faithfulness'))}, "
                        f"answer_relevancy={fmt_score(scores.get('answer_relevancy'))}")

            # 每处理 10 个问题保存一次（防止中途失败丢失数据）
            if (success_count + fail_count) % 10 == 0:
                save_excel_data(df, str(output_path))
                logging.info("已自动保存中间结果")

        except Exception as e:
            logging.error(f"评估问题 [{question_id}] 失败: {e}")
            fail_count += 1
            continue

    # 保存最终结果
    save_excel_data(df, str(output_path))

    # 输出统计信息
    logging.info("=" * 50)
    logging.info(f"评估完成!")
    logging.info(f"总数: {total}")
    logging.info(f"成功: {success_count}")
    logging.info(f"失败: {fail_count}")
    logging.info(f"结果已保存至: {output_path}")
    logging.info("=" * 50)


if __name__ == "__main__":
    asyncio.run(main())
