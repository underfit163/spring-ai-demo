# Spring AI как надо, а не как все

## Модуль 1. Spring AI + Local LLM

1. Обычный POST
```
┌──────────────────────┐
│      Browser         │
│                      │
│  textarea: prompt    │
│  кнопка SEND         │
└──────────┬───────────┘
           │
           │ POST
           │ /chat/{chatId}/entry
           │
           │ prompt=Привет
           ▼
┌────────────────────────────────┐
│         ChatController         │
│                                │
│ @PostMapping(...)              │
│ talkToModel(chatId, prompt)    │
└──────────┬─────────────────────┘
           │
           │ chatFacade
           │ .proceedInteraction()
           ▼
┌────────────────────────────────┐
│          ChatFacade            │
│                                │
│ proceedInteraction(...)        │
└──────────┬─────────────────────┘
           │
           │
           ▼
┌────────────────────────────────┐
│         AI / LLM               │
│                                │
│   генерация полного ответа     │
└──────────┬─────────────────────┘
           │
           │ полный ответ
           ▼
┌────────────────────────────────┐
│          ChatFacade            │
│                                │
│ сохраняет USER + ASSISTANT     │
│ в chat.history                 │
└──────────┬─────────────────────┘
           │
           │ return
           ▼
┌────────────────────────────────┐
│         Controller             │
│                                │
│ redirect:/chat/{chatId}        │
└──────────┬─────────────────────┘
           │
           │ HTTP 302
           ▼
┌──────────────────────┐
│      Browser         │
│                      │
│ GET /chat/{chatId}   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│      Thymeleaf       │
│                      │
│ chat.history         │
│      ↓               │
│ отображает сообщения │
└──────────────────────┘
```

2. Streaming через SSE
```
┌──────────────────────┐
│       Browser        │
│                      │
│ textarea             │
│ "Привет"             │
└──────────┬───────────┘
           │
           │ GET
           │ /chat-stream/5
           │ ?prompt=Привет
           ▼
┌────────────────────────────────┐
│         ChatController         │
│                                │
│ @GetMapping(...)               │
│ produces = text/event-stream   │
└──────────┬─────────────────────┘
           │
           │
           │ SseEmitter
           ▼
┌────────────────────────────────┐
│          ChatFacade            │
│                                │
│ proceedInteractionWithStreaming│
└──────────┬─────────────────────┘
           │
           │ streaming request
           ▼
┌────────────────────────────────┐
│             LLM                │
│                                │
│  "Привет! Конечно..."          │
│                                │
│  ↓ token                       │
│  ↓ token                       │
│  ↓ token                       │
│  ↓ token                       │
└──────────┬─────────────────────┘
           │
           │ tokens
           ▼
┌────────────────────────────────┐
│          SseEmitter            │
│                                │
│ event: message                 │
│ data: {"text":"Пр"}            │
│                                │
│ data: {"text":"ивет"}          │
│                                │
│ data: {"text":"!"}             │
└──────────┬─────────────────────┘
           │
           │ HTTP stream
           ▼
┌────────────────────────────────┐
│            Browser             │
│                                │
│ EventSource                    │
│       ↓                        │
│ event.onmessage                │
│       ↓                        │
│ fullText += token              │
│       ↓                        │
│ marked.parse(fullText)         │
│       ↓                        │
│ отображение ответа             │
└────────────────────────────────┘
```

Итог:
```
                         ┌─────────────────┐
                         │     Browser     │
                         └────────┬────────┘
                                  │
                    ┌─────────────┴──────────────┐
                    │                            │
                    │                            │
              Обычный режим                 Streaming
                    │                            │
                    ▼                            ▼
          POST /chat/{id}/entry       GET /chat-stream/{id}
                    │                            │
                    │                            │
                    ▼                            ▼
          talkToModel()                talkToModel()
                    │                            │
                    ▼                            ▼
       proceedInteraction()       proceedInteractionWithStreaming()
                    │                            │
                    ▼                            ▼
                   LLM                          LLM
                    │                            │
                    │                            │
              полный ответ                  token 1
                    │                       token 2
                    │                       token 3
                    ▼                       token 4
             chat.history                     │
                    │                         │
                    ▼                         ▼
             redirect:/chat/id            SseEmitter
                    │                         │
                    ▼                         ▼
               Thymeleaf                 EventSource
                    │                         │
                    ▼                         ▼
              весь чат                  постепенный
                                       ответ AI
```

## Модуль 2. RAG

## Модуль 3. Оптимизация качества