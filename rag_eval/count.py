#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试脚本：打印当前 Milvus 向量数据库的文档数量
"""

from pymilvus import MilvusClient

# Milvus 配置
MILVUS_CONFIG = {
    "host": "localhost",
    "port": 19530,
    "collection_name": "chat3",
    "username": "root",
    "password": "Milvus"
}


def get_collection_doc_count():
    """
    获取 Milvus 集合中的文档数量

    Returns:
        int: 文档数量
    """
    try:
        # 构建连接 URI
        uri = f"http://{MILVUS_CONFIG['host']}:{MILVUS_CONFIG['port']}"

        # 创建客户端连接
        client = MilvusClient(
            uri=uri,
            user=MILVUS_CONFIG['username'],
            password=MILVUS_CONFIG['password']
        )

        collection_name = MILVUS_CONFIG['collection_name']

        # 检查集合是否存在
        if not client.has_collection(collection_name):
            print(f"集合 '{collection_name}' 不存在")
            return 0

        # 获取集合统计信息
        stats = client.get_collection_stats(collection_name)
        print(f"集合 '{collection_name}' 的统计信息: {stats}")

        # 获取文档数量（row_count）
        doc_count = stats.get('row_count', 0)
        return doc_count

    except Exception as e:
        print(f"连接 Milvus 或获取文档数量失败: {e}")
        import traceback
        traceback.print_exc()
        return -1


def query_sample_data(limit: int = 1):
    """
    查询向量数据库中的示例数据，查看数据格式

    Args:
        limit: 查询的数据条数，默认 1 条

    Returns:
        list: 查询到的数据列表
    """
    try:
        # 构建连接 URI
        uri = f"http://{MILVUS_CONFIG['host']}:{MILVUS_CONFIG['port']}"

        # 创建客户端连接
        client = MilvusClient(
            uri=uri,
            user=MILVUS_CONFIG['username'],
            password=MILVUS_CONFIG['password']
        )

        collection_name = MILVUS_CONFIG['collection_name']

        # 检查集合是否存在
        if not client.has_collection(collection_name):
            print(f"集合 '{collection_name}' 不存在")
            return []

        # 查询数据
        print(f"正在查询集合 '{collection_name}' 中的 {limit} 条数据...")
        results = client.query(
            collection_name=collection_name,
            filter="",
            output_fields=["*"],
            limit=limit
        )

        return results

    except Exception as e:
        print(f"查询数据失败: {e}")
        import traceback
        traceback.print_exc()
        return []


def main():
    """主函数"""
    print("=" * 60)
    print("Milvus 向量数据库信息查询")
    print("=" * 60)
    print(f"连接配置:")
    print(f"  - Host: {MILVUS_CONFIG['host']}")
    print(f"  - Port: {MILVUS_CONFIG['port']}")
    print(f"  - Collection: {MILVUS_CONFIG['collection_name']}")
    print("-" * 60)

    doc_count = get_collection_doc_count()

    print("-" * 60)
    if doc_count >= 0:
        print(f"[OK] 当前文档数量: {doc_count}")
    else:
        print(f"[FAIL] 查询失败")
    print("=" * 60)

    # 查询示例数据
    print("\n" + "=" * 60)
    print("示例数据查询")
    print("=" * 60)

    results = query_sample_data(limit=1)

    if results:
        print(f"查询到 {len(results)} 条数据:")
        print("-" * 60)
        for i, record in enumerate(results, 1):
            print(f"\n【第 {i} 条数据】")
            for field, value in record.items():
                # 跳过 vector 字段（太长不实用）
                if field == "vector":
                    continue
                # 截断过长的内容
                if isinstance(value, str) and len(value) > 200:
                    value = value[:200] + "..."
                print(f"  {field}: {value}")
    else:
        print("未查询到数据")

    print("\n" + "=" * 60)

    return doc_count


if __name__ == "__main__":
    main()

