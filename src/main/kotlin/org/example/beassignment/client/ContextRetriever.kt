package org.example.beassignment.client

/**
 * 사용자 질문과 관련된 문서 컨텍스트를 검색하는 인터페이스.
 * 향후 벡터 DB(Pinecone, Weaviate 등)를 연동하여 RAG 파이프라인을 구성할 때
 * 이 인터페이스의 구현체만 교체하면 된다.
 */
interface ContextRetriever {

    suspend fun retrieve(query: String, maxResults: Int = 5): List<String>
}
