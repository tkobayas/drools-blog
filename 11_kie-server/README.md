# 11 Kie Server
ここまでルールエンジンを実行するために直接 Java コードを書いてましたが、REST API での実行も可能です。その場合 REST エンドポイントを公開するための Web アプリが必要ですね。そのために Kie Server という Web アプリが提供されています。Kie Server は以下のようなことができます。

- KJAR のデプロイ、スタート、ストップ（Business Central または REST API で管理）
- KJAR のリモート実行（ルール、プロセス、ソルバー）
- REST または JMS に対応
- ペイロードは JAXB, JSON, XSTREAM に対応
- 上記プロトコルをラップした Java クライアント API を提供

などなど。

Kie Server はアプリケーションサーバーにデプロイし、セントラルルールエンジンとして動作します。クライアントは REST API でファクトをインサート、ルール実行、結果の取得を行います。ルールエンジンを Kie Server 側にすることでクライアントは CPU/メモリ 消費を分離、軽量化することができます。

では使ってみましょう。

前回 [link] 10 Business Central で使った WildFly に Kie Server をデプロイしていきます。WildFly + Business Central のセットアップは前回記事を参照してください！

こちらのリンクの「KIE Server WARS」から「ee7, ee8, webc WAR」をクリックし、「ee8」の WAR をダウンロードします。

[https://www.drools.org/download/download.html]

2020年3月現在で 7.33.0.Final が最新バージョンですが、前回から 7.32.0.Final で進めているのでここでは 7.32.0.Final の WAR をダウンロードします（が、後からこのブログを読んだ人は最新バージョンを使ってね！）。 7.32.0.Final の WAR のリンクはこちらです。

https://repo1.maven.org/maven2/org/kie/server/kie-server/7.32.0.Final/kie-server-7.32.0.Final-ee8.war

さて、kie-server-7.32.0.Final-ee8.war をデプロイする前に2つやることがあります。

1) 名前変更

2) system property

wildfly-14.0.1.Final/standalone/deployments の下に先ほどダウンロードした kie-server.war をコピーします。

ユーザは前回記事で作成したとおりです。ロール kie-server を持たせるのを忘れないように。

では WildFly を起動します。Kie Server が MDB を使用するので、今回は standalone-full.xml を使います。

```
$ ./standalone.sh -c standalone-full.xml
```

デプロイには少々時間がかかります。。。

```
17:51:25,241 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 45) WFLYSRV0010: Deployed "kie-server.war" (runtime-name : "kie-server.war")
```

まずは Business Central が Kie Server を認識しているか確認します。

```
http://localhost:8080/business-central/
```

[メニュー]->[デプロイ]->[実行サーバー]

image

TODO:

```
curl -u rhdmAdmin:password1! -X POST -H 'Content-Type: application/json' http://localhost:8080/kie-server/services/rest/server/containers/nstances/helloProject_1.0.0-SNAPSHOT -d '{"lookup":"myksession","commands":[{"insert":{"object":{"com.myspace.helloproject.Person":{"name":"太郎","age":30,"adult":false}},"out-identifier":"fact-1"}}]}'
```
