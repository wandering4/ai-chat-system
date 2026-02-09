前置依赖:
1. jdk21
2. xzf-blog 项目正常运行（同时说明mysql、redis、rocketmq、nacos正常）
3. 前往xzf-blog 项目的feature/Framework-jdk21-adapt分支,blog-Framework模块执行git install构建jdk21的blog-commons依赖 
4. 修改milvus数据库连接为你可用的（可以直接cd docs;docker-compose -d;快速部署） 
5. ai-chat-exporter/src/main/resources/config/application-dev.yml中添加你自己的system.chat.deepseek.api-key和langchain4j.embedding.model.api-key（自行前往deepseek和智谱官网申请）

mvn test无问题后即可成功运行