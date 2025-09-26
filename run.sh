#!/bin/bash
echo "🔐 환경변수 설정 중..."

if [ -f .env ]; then
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

if [ -f .env.local ]; then
    export $(grep -v '^#' .env.local | grep -v '^$' | xargs)
    echo "✅ 로컬 API 키 로드됨"
else
    echo "⚠️  .env.local 파일이 없습니다. API 키를 환경변수로 설정하거나 .env.local 파일을 생성하세요."
fi

if [ ! -z "$OPENAI_API_KEY" ]; then
    echo "✅ OpenAI API 키: ${OPENAI_API_KEY:0:10}..."
else
    echo "❌ OpenAI API 키가 설정되지 않았습니다!"
fi

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/codebattlearena
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password

echo "🚀 Spring Boot 애플리케이션 시작..."
./mvnw spring-boot:run