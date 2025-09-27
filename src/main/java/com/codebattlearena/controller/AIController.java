package com.codebattlearena.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        
        // 간단한 규칙 기반 응답 (실제 AI API 대신)
        String response = generateResponse(message);
        
        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
    
    private String generateResponse(String message) {
        String msg = message.toLowerCase();
        
        if (msg.contains("자바") || msg.contains("java")) {
            return "자바는 객체지향 프로그래밍 언어입니다. 클래스와 객체, 상속, 다형성 등의 개념을 익히시면 됩니다. 어떤 부분이 궁금하신가요?";
        } else if (msg.contains("배열") || msg.contains("array")) {
            return "배열은 같은 타입의 데이터를 연속적으로 저장하는 자료구조입니다.\n\n예시:\nint[] arr = new int[5];\narr[0] = 10;\n\n배열의 크기는 생성 시 고정되며, 인덱스는 0부터 시작합니다.";
        } else if (msg.contains("반복문") || msg.contains("for") || msg.contains("while")) {
            return "반복문에는 for, while, do-while이 있습니다.\n\n// for문 예시\nfor(int i=0; i<10; i++) {\n    System.out.println(i);\n}\n\n// while문 예시\nint i = 0;\nwhile(i < 10) {\n    System.out.println(i);\n    i++;\n}";
        } else if (msg.contains("클래스") || msg.contains("class")) {
            return "클래스는 객체를 만들기 위한 설계도입니다.\n\npublic class Student {\n    private String name;\n    private int age;\n    \n    public Student(String name, int age) {\n        this.name = name;\n        this.age = age;\n    }\n    \n    public String getName() {\n        return name;\n    }\n}";
        } else if (msg.contains("상속") || msg.contains("extends")) {
            return "상속은 기존 클래스의 속성과 메서드를 새로운 클래스에서 재사용하는 것입니다.\n\npublic class Animal {\n    public void eat() {\n        System.out.println(\"먹는다\");\n    }\n}\n\npublic class Dog extends Animal {\n    public void bark() {\n        System.out.println(\"멍멍\");\n    }\n}";
        } else if (msg.contains("안녕") || msg.contains("hello")) {
            return "안녕하세요! 저는 CodeBattleArena의 AI 튜터입니다. 자바 프로그래밍에 대해 궁금한 것이 있으시면 언제든 물어보세요!";
        } else if (msg.contains("도움") || msg.contains("help")) {
            return "다음과 같은 주제들에 대해 도움을 드릴 수 있습니다:\n\n• 자바 기초 문법\n• 객체지향 프로그래밍\n• 배열과 컬렉션\n• 반복문과 조건문\n• 클래스와 메서드\n• 상속과 다형성\n\n구체적인 질문을 해주세요!";
        } else {
            return "좋은 질문이네요! 더 구체적으로 설명해주시면 정확한 답변을 드릴 수 있습니다. 예를 들어:\n\n• \"자바에서 배열을 어떻게 선언하나요?\"\n• \"for문 사용법을 알려주세요\"\n• \"클래스와 객체의 차이점은 무엇인가요?\"\n\n이런 식으로 질문해주세요!";
        }
    }
}
