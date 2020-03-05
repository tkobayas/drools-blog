# 11 Kie Server
ここまでルールエンジンを実行するために直接 Java コードを書いてましたが、REST API での実行も可能です。その場合 REST エンドポイントを公開するための Web アプリが必要です。そのために Kie Server という Web アプリが提供されています。Kie Server は以下のようなことができます。

- KJAR のデプロイ、アンデプロイ、スタート、ストップ（Business Central または REST API で管理）
- KJAR のリモート実行（ルール、プロセス、ソルバー）
- REST または JMS に対応
- ペイロードは JAXB, JSON, XSTREAM に対応
- 上記プロトコルをラップした Java クライアント API を提供

などなど。

Kie Server はアプリケーションサーバーにデプロイすることで、ルールエンジンサーバーとして動作します。クライアントは REST API でファクトをインサート、ルール実行、結果の取得を行います。ルールエンジンを Kie Server 側にすることでクライアントは CPU/メモリ消費を分離、軽量化することができます。

Kie Server は単独でも利用可能ですが、Business Central により GUI 管理することができます。Kie Server と Business Central は異なるサーバーで運用可能で、本番稼動時にはそのほうがおすすめですが、開発時には同一サーバーで利用もできます。

では使ってみましょう。

前回 [10 Business Central](https://tokobayashi.hatenablog.com/entry/2020/02/07/155603) で使った WildFly に Kie Server をデプロイしていきます。WildFly + Business Central のセットアップは前回記事を参照してください！

こちらのリンクの「KIE Server WARS」から「ee7, ee8, webc WAR」をクリックし、kie-server-7.XX.0.Final-ee8.war をダウンロードします。

[https://www.drools.org/download/download.html]

2020年3月現在で 7.33.0.Final が最新バージョンですが、前回記事で 7.32.0.Final を使って進めているのでここでは 7.32.0.Final の WAR をダウンロードします（が、後からこのブログを読んだ人は最新バージョンを使ってね！）。 7.32.0.Final の WAR のリンクはこちらです。

https://repo1.maven.org/maven2/org/kie/server/kie-server/7.32.0.Final/kie-server-7.32.0.Final-ee8.war

wildfly-14.0.1.Final/standalone/deployments の下に WAR をコピーします。

さて、WildFly を起動する前に2つやることがあります。

1) WAR名変更

アクセス時の URL を簡単にするために WAR 名を変更します。必須ではないですが、変更しない場合、以下の説明の URL は適宜読み替えて下さい。

- business-central-7.32.0.Final-wildfly14.war -> business-central.war
- kie-server-7.32.0.Final-ee8.war -> kie-server.war

2) システムプロパティ

以下のシステムプロパティを standalone-full.xml に追加してください。ユーザは前回記事で作成したとおりです。ロール kie-server を持たせておくのを忘れないように。パスワードはサンプルなので平文ですが、もちろん隠すことも出来ます。詳しくは WildFly のドキュメントを参照ください。

```
  <system-properties>
    <property name="org.kie.server.controller" value="http://localhost:8080/business-central/rest/controller"/>
    <property name="org.kie.server.controller.user" value="rhdmAdmin"/>
    <property name="org.kie.server.controller.pwd" value="password1!"/>
    <property name="org.kie.server.id" value="default-kieserver"/>
    <property name="org.kie.server.location" value="http://localhost:8080/kie-server/services/rest/server"/>
    <property name="org.kie.server.user" value="rhdmAdmin"/>
    <property name="org.kie.server.pwd" value="password1!"/>
  </system-properties>
```

では WildFly を起動します。Kie Server が MDB を使用するので、今回は standalone-full.xml を使います。

```
$ ./standalone.sh -c standalone-full.xml
```

起動には少々時間がかかります。。。Kie Server デプロイ後もごにょごにょログがでます。

```
17:51:25,241 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 45) WFLYSRV0010: Deployed "kie-server.war" (runtime-name : "kie-server.war")
...
```

まずは Business Central が Kie Server を認識しているか確認してみましょう。

```
http://localhost:8080/business-central/
```

[メニュー]->[デプロイ]->[実行サーバー] で確認できます。"default-kieserver" が見えるはずです。この名前はシステムプロパティで設定したものです。

[f:id:tokobayashi:20200305160150p:plain]

さて、前回記事でプロジェクト helloProject を作っていたのでそれをデプロイします。プロジェクトが無い人は前回記事を参考に作ってください（テストシナリオは不要です）。

そしてデプロイ前に1点、プロジェクトに追加することがあります。それはステートレス ksession を設定することです。「実行して結果を受け取って終了」というユースケースではステートレス ksession のほうが適切です。また Kie Server ではそのような使い方が推奨です。Kie Server でステートフル ksession を使うことも可能ですが、障害時のハンドリングなどが困難になります。ステートレス ksession については [04 ステートレス ksession](https://tokobayashi.hatenablog.com/entry/2019/05/15/174642) をご覧ください。

さてステートレス ksession の設定ですが、Business Central で以下のように進めます。

- プロジェクトの [設定]タブ -> [KIE bases] から「KIE ベースの追加」をクリック
- 名前に「mykbase」と入力
- その kbase 設定の右のほうに「KIE sessions」というリンクがあるのでそれをクリック
- ポップアップが出るので、「KIE session を追加」をクリック
- 名前に「myksession」と入力
[f:id:tokobayashi:20200305160155p:plain]
- その右のボックスにはデフォルトで「stateless」とあるのでそのままでOK
- 「完了」をクリック。ポップアップが閉じる
- 左下の「保存」をクリック

ここまで来たらビルドして Kie Server にデプロイします。右上の「デプロイ」ボタンをクリックしてください。成功すれば「サーバー設定へのデプロイに成功し、コンテナの開始に成功しました。」というポップアップがでます。「helloProject_1.0.0-SNAPSHOT」という名前のデプロイメントユニット（コンテナとも呼ばれます）が作成され、開始されていることも確認できます。

[f:id:tokobayashi:20200305160158p:plain]

では実行してみましょう！

以下の curl コマンドで、Person オブジェクトを insert します。明示的に fireAllRules と言っていませんが、ステートレスの場合、自動的に fireAllRules もやってくれます。
```
curl -u rhdmAdmin:password1! -X POST -H 'Content-Type: application/json' http://localhost:8080/kie-server/services/rest/server/containers/instances/helloProject_1.0.0-SNAPSHOT -d '{"lookup":"myksession","commands":[{"insert":{"object":{"com.myspace.helloproject.Person":{"name":"太郎","age":30,"adult":false}},"out-identifier":"fact-1"}}]}'
```
次のようなレスポンスが返ってきます。
```
{
  "type" : "SUCCESS",
  "msg" : "Container helloProject_1.0.0-SNAPSHOT successfully called.",
  "result" : {
    "execution-results" : {
      "results" : [ {
        "value" : {"com.myspace.helloproject.Person":{
  "name" : "太郎",
  "age" : 30,
  "adult" : true
}},
        "key" : "fact-1"
      } ],
      "facts" : [ {
        "value" : {"org.drools.core.common.DefaultFactHandle":{
  "external-form" : "0:1:155540311:155540311:1:DEFAULT:NON_TRAIT:com.myspace.helloproject.Person"
}},
        "key" : "fact-1"
      } ]
    }
  }
}
```
ルールが実行されて adult が true になってますね。
