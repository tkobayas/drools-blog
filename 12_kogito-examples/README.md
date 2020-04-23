# 12 Kogito Examples
Kogito 0.9.1 がリリースされました！

[f:id:tokobayashi:20200423143925p:plain]

別に切りのいい数字じゃないって？ や、そうなんですけど、0.9.1 はドキュメントや examples を整備して「いい感じに仕上げました」マイルストーンリリースなのです。今後もバージョン自体はどんどん上がっていくのですが(0.10.0 もう出た)、今まで様子見だったひともここらで手を出してみては？というところです。

ドキュメントはこちら。英語だけど https://www.deepl.com/translator にぶっこめば OK！

https://docs.jboss.org/kogito/release/latest/html_single/

今回は Kogito がどんなものか体験するため kogito-examples をざっと紹介します。

まず clone します。デフォルトで stable ブランチになっています。このブランチが最新安定バージョン（現時点で 0.9.1）です。

```
git clone https://github.com/kiegroup/kogito-examples.git
cd kogito-examples
```
中にはたくさんのサンプルがあります。どのサンプルにも README.md があり、実行コマンド、テスト用の curl コマンドが記載されています。

Quarkus 用のサンプルを紹介しますが、それぞれ対になる Spring Boot 用のサンプルもあります。内容はほとんど同じで、起動コマンドが違うだけです。

## ruleunit-quarkus-example

基本的なルール実行サービスです。ローン申請ファクト（LoanApplication）を POST すると、承認された申請が返ってきます。

```
mvn clean compile quarkus:dev
```

で、起動。起動したら次の curl コマンドでテストできます。

```
curl -X POST -H 'Accept: application/json' -H 'Content-Type: application/json' -d '{"maxAmount":5000,"loanApplications":[{"id":"ABC10001","amount":2000,"deposit":100,"applicant":{"age":45,"name":"John"}}, {"id":"ABC10002","amount":5000,"deposit":100,"applicant":{"age":25,"name":"Paul"}}, {"id":"ABC10015","amount":1000,"deposit":100,"applicant":{"age":12,"name":"George"}}]}' http://localhost:8080/find-approved
```

ソースコードを説明すると

- RuleUnitQuery.drl : ルールを記述する DRL。ポイントは「unit LoanUnit;」という宣言です。これで LoanUnit クラスと組み合わせて、サービス全体を生成できるようにします。ルールの「/loanApplications[]」という記法は LoanUnit クラスの DataStore loanApplications からファクトを評価するということですが、通常の DRL での「LoanApplication()」パターンと同じだと思っていいです。また DRL に「query」も記述されています。これが上記テストで使用した REST エンドポイントを生成します。
- LoanApplication.java : ローン申請ファクト
- Applicant.java : 申請者情報。LoanApplication のただのプロパティです
- LoanUnit.java : RuleUnitData を implements します。今までの Drools では使われていなかった（実際には存在していましたが）ルールユニットという仕組みです。仕組みといっても簡単にいうとルールとファクトを結び付けるもので、 DataStore というクラスでファクトをラップするだけです。「maxAmount」は DRL で global として扱われます。この情報を DRL 側に書いて、このクラスを不要にする記法もあります。

その他の必要なコードは quarkus:dev で生成されます。target/ の下を見てみると面白いでしょう。

README.md には dev じゃない実行や GraalVM による native ビルドについても記載されています。

OpenShift/Red Hat CodeReady Container 上での実行についてもまた後日書くつもりですが、手順のみならこちら(https://tkobayas.wordpress.com/2020/03/23/simple-steps-to-run-kogito-on-red-hat-codeready-containers/)を参照ください

## dmn-quarkus-example

DRL の代わりに DMN を使ったサービスです。DMN についてはまた後日ブログを書く予定ですが、とりあえず実行してみましょう。交通違反情報を POST すると罰金や免許停止かどうかが返ってきます。

```
mvn clean compile quarkus:dev
```

起動したら

```
curl -X POST -H 'Accept: application/json' -H 'Content-Type: application/json' -d '{"Driver":{"Points":2},"Violation":{"Type":"speed","Actual Speed":120,"Speed Limit":100}}' http://localhost:8080/Traffic%20Violation
```

ソースコードは dmn ファイル1枚だけ！

- Traffic Violation.dmn : DMN はエディターで閲覧、編集します。VSCode のエクテンションがおすすめです(https://docs.jboss.org/kogito/release/latest/html_single/#con-kogito-modelers_kogito-creating-running)。オンラインエディターもあります(https://kiegroup.github.io/kogito-online/#/)。開くとルールの構成が簡単に分かりますね。他のクラスが必要無いのは DMN 自体に型情報が定義されていること、また入出力は Map ベースで行っており、カスタム Java クラスが必要無いからです（カスタム Java クラスを用いた入出力も開発中）。

[f:id:tokobayashi:20200423144022p:plain]

## process-script-example

スクリプトタスクのみのシンプルなプロセスサービス。お気づきかもしれませんが、Drools や jBPM といった名前は前面には出していません（実際に依存ライブラリ名には出てきますが）。ルールもプロセスもひっくるめて Kogito という考え方です。

```
mvn clean compile quarkus:dev
```

起動したら

```
curl -X POST -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{"name" : "john"}' http://localhost:8080/scripts
```

簡単過ぎですかね。これをベースにプロセスを拡張していってもいいでしょう。こちらもソースは bpmn ファイル一枚のみ。

- scripts.bpmn : BPMN も VSCode のエクテンションやオンラインエディターで閲覧、編集します。jBPM/BPMN 関連もまた後日詳しく書きたいなあー

[f:id:tokobayashi:20200423144037p:plain]

## その他

ruleunit-quarkus-example のデシジョンテーブル版 decisiontable-quarkus-example や、ルールとプロセスを融合した process-quarkus-example , process-business-rules-quarkus 、プランニングの process-optaplanner-springboot などなどあります。

一番本格的なやつは kogito-travel-agency で、Infinispan による永続化、Kafka によるメッセージ連携、データを GraphQL で取得などさまざまな機能を体験できます。OpenShift にデプロイするにはドキュメントの以下のセクションを参照してください。

https://docs.jboss.org/kogito/release/latest/html_single/#con-kogito-travel-agency_kogito-deploying-on-openshift
