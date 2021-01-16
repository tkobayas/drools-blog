# 13 DMN
DMN (Decision Model and Notation) とは OMG (Object Management Group) で策定されたビジネスルールについての仕様です。

https://www.omg.org/dmn/

ビジネスプロセス仕様 BPMN (Business Process Model and Notation) のルール版だと思ってよいでしょう。

さて、DMN と、この Drools ブログでここまでやってきた DRL とはどう関係するのでしょう？ 答えは｡｡｡別物です！ 文法も違いますし、実行エンジンも違います。

ですので DRL をやってきた人にとっては新しい記述法を覚えなければいけないわけで、どのようなメリットがあるかが最初の関心事でしょう。初めて Drools を使う人であれば DRL か DMN のどっちを使えばいいのか？という疑問が出ますね。では以下に簡単に説明します。

DMN のメリット
* ルールがビジュアル化される
* 標準仕様である

DMN のデメリット (DRL と比較した場合)
* ステートレス実行のみ
* 推論(Inference)が使えない [https://tokobayashi.hatenablog.com/entry/2019/05/06/160337]

といったところです。

個人的には「ルールがビジュアル化される」が非常に大きいと考えられます。BPMN の場合、ビジネスプロセスがビジュアル化されるわけですが、これはどの開発プロジェクトでも使用ツールはともかく(パワポやVisioなど)、「業務プロセス」のビジュアル化はされていることでしょう。でも業務ルールのビジュアル化ってあんまりやってないのでは？詳細設計書に文章でずらずら書かれているとか。。。DMN で概要をいったんビジュアル化するだけでも新しい地平が開けるのではないでしょうか。

では実際に使ってみましょう。サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 13_DMN です。お題はドライバーが交通違反した時の罰金＆免停です。

プロジェクトの中身がめっちゃ少ない事に気付くでしょうか。実質 DMN を定義する Traffic_Violation.dmn とそれを実行するテストケースの DMNTest.java だけです。ルールで使う型情報は Traffic_Violation.dmn の中に定義されています。

ところで DMN ファイルは xml ですが、直接編集する事を想定していません。編集用のエディターがあります。推奨は VSCode のエクステンション「DMN Editor」です。Extensions から検索して簡単にインストールできます。また、お手軽な Web 上のエディターもあります。

https://kiegroup.github.io/kogito-online/#/

こちらの「Edit existing file」で、ファイルをアップロードしたり、「Open from source」に直接URL（例： https://github.com/tkobayas/drools-blog/blob/master/13_DMN/src/main/resources/Traffic_Violation.dmn） を突っ込んでも開けます。

さて、DMN Editor で開くとこんな感じです。

[image01]

下の2つの楕円「ドライバー」「違反」が Input Data、つまりルールに入力されるデータで、2つの長方形「罰金」「免許停止」が Decision、つまりルールです。矢印はデータの依存関係を表します。例えば「罰金」ルールの結果は「免許停止」ルールの入力にもなる、ということです。

「罰金」をクリックし、その左側に出るアイコン Edit をクリックしてみてください。デシジョンテーブルが開きますね。

[image02]

見たまんま、違反データの中身に応じて、「罰金」データを生成します。

同様に、「免許停止」を Edit で開くと

[image03]

今度はデシジョンテーブルではなく、式が記述されています。これは FEEL という言語で、簡単に計算等のロジックを記述できます。最終的に「免許停止」が "はい" か "いいえ" を出力する、というのが分かりますね。

あと、エディタの上部にある「Data Types」をクリックしてみましょう。

[image04]

これがこの DMN の中で使う型情報です。例えば「違反」には tViolation という型を定義して、判定に必要なフィールドを持たせています。

次にこれを実行する Java コードを見てみましょう。

https://github.com/tkobayas/drools-blog/blob/master/13_DMN/src/test/java/org/example/DMNTest.java

ttps://github.com/tkobayas/drools-blog/blob/master/02_decisiontable/src/test/java/org/example/DroolsTest.java

```java
        final KieServices ks = KieServices.Factory.get();
        final KieContainer kieContainer = KieHelper.getKieContainer(
                                                                    ks.newReleaseId("com.sample", "dmn-example-" + UUID.randomUUID(), "1.0"),
                                                                    ks.getResources().newClassPathResource("Traffic_Violation.dmn", DMNTest.class));

        DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
```
DRL と同じように KieServices や KieContainer を使いますが、最終的に利用するメインのオブジェクトは KieSession ではなく DMNRuntime になります。


```java
        final DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF", "Traffic Violation");
```

Namespace と Name を指定して（エディタの Properties -> Definitions を見れば分かります）、DMNModel を取得します。これが Traffic_Violation.dmn をオブジェクト化したものです。

```java
        final DMNContext context = DMNFactory.newContext();

        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("名前", "太郎");
        driverMap.put("年齢", 34);
        driverMap.put("ポイント", 18);
        context.set("ドライバー", driverMap);

        Map<String, Object> violationMap = new HashMap<>();
        violationMap.put("日付", LocalDate.now());
        violationMap.put("タイプ", "速度超過");
        violationMap.put("制限速度", 100);
        violationMap.put("実際の速度", 120);
        context.set("違反", violationMap);

        final DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, context);
```

入力データは Map で作成し、 DMNContext に set します。あとは dmnRuntime.evaluateAll() を呼ぶだけです。

DMNResult から結果を取得できます。単純に String として表示するだけでも処理結果がわかるでしょう。
```
DMNResultImpl{context={
    ドライバー: {
        年齢: 34
        名前: 太郎
        ポイント: 18
    }
    違反: {
        実際の速度: 120
        制限速度: 100
        日付: 2021-01-16
        タイプ: 速度超過
    }
    罰金: {
        金額: 500
        ポイント: 3
    }
    免許停止: はい
}
, messages=org.kie.dmn.core.util.DefaultDMNMessagesManager@695a69a1}
```

今日はここまで！

DMN の書き方についてはこのドキュメントがおすすめです。

https://access.redhat.com/documentation/ja-jp/red_hat_decision_manager/7.9/html-single/getting_started_with_red_hat_decision_manager/index#assembly-getting-started-decision-services