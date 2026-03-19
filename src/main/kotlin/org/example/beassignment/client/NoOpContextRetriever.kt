package org.example.beassignment.client

import org.springframework.stereotype.Component

/**
 * RAG 미적용 시 사용되는 기본 구현체.
 * 항상 빈 리스트를 반환하여 기존 채팅 동작에 영향을 주지 않는다.
 * 벡터 DB 도입 시 이 클래스를 대체하는 구현체를 등록하면 된다.
 */
@Component
class NoOpContextRetriever : ContextRetriever {

    override suspend fun retrieve(query: String, maxResults: Int): List<String> = emptyList()
}
