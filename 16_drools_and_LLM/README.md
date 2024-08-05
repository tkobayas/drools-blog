# 16 Drools と LLM
昨今 LLM (Large Language Model) が大流行りですね。「LLMで何かアプリ作れや」と言われている方も多いのではないでしょうか。しかし、LLM だけで上手く行くのか？

AIチャットボットが誤った返金規程を提示し、裁判になったエア・カナダのニュースをご存知でしょうか。

https://news.yahoo.co.jp/articles/7251adcc317037b877ad04653b282b85ba4c3f5b

LLM が生成する文章が非常に自然で役に立つのは間違いないですが、「間違ったことを書くことがある」ということはよく知られてますね。ビジネスの根幹となるルールを　LLM　に任せるのは危険です。そうです、ルールはルールエンジンに任せましょう。

こちらが Mario Fusco による Drools と LLM を組み合わせたアプリケーションのサンプルプロジェクトです。
https://github.com/mariofusco/quarkus-drools-llm/

このプロジェクトには、「ローン申し込み」「パスワード生成」「フライト返金チャット」の３つのサンプルが含まれています。このうち「フライト返金チャット」について、私が日本語化したフォークがあるのでこちらで見てみましょう。

https://github.com/tkobayas/quarkus-drools-llm/tree/openai-ja

環境: JDK 21+

実行手順
```sh
git clone https://github.com/tkobayas/quarkus-drools-llm.git
cd quarkus-drools-llm
git checkout openai-ja
export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY=demo
export QUARKUS_LANGCHAIN4J_OPENAI_HOTMODEL_API_KEY=demo
./mvnw compile quarkus:dev
```

上記の環境変数 `QUARKUS_LANGCHAIN4J_OPENAI_API_KEY` と `QUARKUS_LANGCHAIN4J_OPENAI_HOTMODEL_API_KEY` は、OpenAI の API キーです。LangChain4Jによる無料のデモキーを使っていますが、自分のキーを使うこともできます。デモキーはデモンストレーション用であり、制限がありますが、このサンプルアプリケーションでは十分です。

アプリケーションが起動したら、ブラウザで http://localhost:8080 にアクセスしてください。以下の画面が表示されます。

[image]

日本語化しているのは3つめの Airline refund chatbot だけなので、そのリンクをクリックしてください。

チャット画面が表示されます。自分が乗ったフライトが遅延して到着したと想定して、返金されるかチャットボットに尋ねてみましょう。

[image]

## 実装

このサンプルアプリケーションは、Quarkus と Drools と LangChain4J を使っています。LangChain4J は様々な LLM を使うためのライブラリです。このブランチ `openai-ja` では OpenAI の GPT-3.5-turbo を使っています。 main ブランチでは Ollama をローカルにインストールして使うよう設定されています。興味があれば、README.md を参照してください。

Quarkus は REST エンドポイントを公開することに加え、`quarkus-langchain4j-openai` で　LangChain4J　をさらに簡単に使うための機能を提供しています。様々な設定が `application.properties` に集約されています。

さて、 Drools と LLM はどのように組み合わされているのでしょうか。

https://github.com/tkobayas/quarkus-drools-llm/tree/openai-ja/src/main/java/org/hybridai/refund　以下のクラスを眺めてみてください。

2つのチャットサービスがあります。こちらは ChatGPT などでよく使うプロンプトをアノテーションで定義しています。`chat` メソッドを呼べば LangChain4J が LLM (今回は OpenAI の API) と通信して返答を得ます。

```java
@RegisterAiService(chatMemoryProviderSupplier = StatefulChat.MemorySupplier.class)
@Singleton
public interface CustomerChatService {

    @SystemMessage("<<SYS>>あなたは航空会社のチャットボットです。あなたの目的は、質問をして顧客の情報を収集することです</SYS>>")
    @UserMessage("""
        顧客の名前と年齢について質問してください。

        +++
        {message}
        +++
        """)
    String chat(@MemoryId String sessionId, String message);

}
```

また２つの Extractor があります。これは LLM の返答から、データを Java オブジェクトにマッピングして生成するためのクラスです。

```java
@RegisterAiService(chatMemoryProviderSupplier = StatelessChat.MemorySupplier.class)
@Singleton
public interface CustomerExtractor {

    @UserMessage("顧客の情報をこのテキストから抽出してください '{text}'。レスポンスは JSON フォーマットの顧客のデータのみです。他の文は含めないでください。" +
            "日本人の氏名は「姓」「名」の順に記載されていることが一般的です。")
    Customer extractData(String text);
}
```

さて、LLM　によるやりとりから必要な情報が得られたら、`DroolsRefundCalculator`　が Drools を呼び出し、ルールに従って処理を行います。こちらがそのルールです。

```java
rule "遅延による返金対象判定" when
	Flight( $delay : delayInMinutes >= 60 )
then
	insert(new RefundAmount( 20 * $delay ));
end

rule "高齢者向け返金増額" when
	Customer( age > 65 )
	$r: RefundAmount()
then
	$r.setAmount( $r.getAmount() * 1.1 );
end
```

極めて明解ですね。明示的なルールがあるのだから、ここは LLM ではなく Drools が処理します。

## さらに詳しく
- こちらの動画は、Nicole Prentzas と Mario Fusco による機械学習とシンボリック推論（= ルールエンジン）の組み合わせについてのセッションです。上記のチャットボットに加え、「機械学習からルールを生成、改善する」というユースケースにも触れています。
  - [VDTRIESTE24] Machine Learning + Symbolic Reasoning - Conference by Nicole Prentzas and Mario Fusco
https://www.youtube.com/watch?v=rCxQmxObZS4

- ニューラルネットワークとシンボリックAIは　Neuro-symbolic AI　と呼ばれ研究されています。
  - https://en.wikipedia.org/wiki/Neuro-symbolic_AI

- LLM とルールエンジンの組み合わせ方のバリエーションについて書かれている記事です。
  - https://medium.com/@pierrefeillet/approaches-in-using-generative-ai-for-business-automation-the-path-to-comprehensive-decision-3dd91c57e38f