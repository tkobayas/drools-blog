# 06 Quarkus で Drools/jBPM を動かす Kogito
これまで入門ネタを書いてきましたが、今回はちょっと最新情報を！

Drools/jBPM には Kogito という新規プロジェクトがあり、Drools/jBPM を軽量に適用するアプローチを目指しています。そしてその中心となるのが Quarkus との組み合わせです。

Quarkus についてはこちらを参照ください！

https://www.slideshare.net/agetsuma/quarkus

で、この Quarkus で Kogito を使うガイドがこちらです。

https://quarkus.io/guides/kogito-guide

ハンズオン的にアプリの作成、実行がガイドされていますが、そもそも動くモノが "using-kogito" にあるので、以下、手っ取り早く実行するための手順を書いていきます。

```
git clone https://github.com/quarkusio/quarkus-quickstarts.git
cd quarkus-quickstarts/using-kogito
./mvnw clean compile quarkus:dev
```

はい、これで dev モードで起動します。

ビルドはともかく、Quarkus は 3 秒くらいで起動します。速い！
```
2019-06-28 17:49:15,946 INFO  [io.quarkus] (main) Quarkus 0.18.0 started in 3.015s. Listening on: http://[::]:8080
```

REST アクセスでアプリを動作させます。Droolsによるルール、jBPMによるプロセスに沿って動作します。詳しくは上記ガイドを参照してください。"using-kogito" の中のソースを見れば分かりますが、とてもシンプルです。

1) Person を POST します。20才なのでルールで Adult と判定され、プロセスを通過して終了します。
```
curl -X POST http://localhost:8080/persons \
    -H 'content-type: application/json' \
    -H 'accept: application/json' \
    -d '{"person": {"name":"John Quark", "age": 20}}'
```

2) アクティブなプロセスを GET します。先程のプロセスは終了したのでレスポンスは空です。
```
curl -X GET http://localhost:8080/persons \
    -H 'content-type: application/json' \
    -H 'accept: application/json'
```
3) 別の Person を POST します。15才なのでルールで Child と判定され、"Special handling for children" タスクで停止します。
```
curl -X POST http://localhost:8080/persons \
    -H 'content-type: application/json' \
    -H 'accept: application/json' \
    -d '{"person": {"name":"Jenny Quark", "age": 15}}'
```

4) もう一度 GET で確認すると id:2 のアクティブなプロセスインスタンスがあることがわかります。
```
curl -X GET http://localhost:8080/persons \
    -H 'content-type: application/json' \
    -H 'accept: application/json'
```
```
[{"id":2,"person":{"adult":false,"age":15,"name":"Jenny Quark"}}]
```
5) 以下の POST でタスクを complete します。プロセスが進行し、終了します。
```
curl -X POST http://localhost:8080/persons/2/ChildrenHandling/1 \
    -H 'content-type: application/json' \
    -H 'accept: application/json' \
    -d '{}'
```

Ctrl+C で quarkus を停止します。

まだ終わりませんよ！

次はネイティブビルドしてみましょう。現時点(2019/06/29)で、このデモはネイティブビルドに GraalVM 19.0.2+ が必要です。https://www.graalvm.org/downloads/ からダウンロードしましょう(CE でも EE でいいです)

$GRAALVM_HOME/bin に $PATH を通し、さらに native-image コンポーネントをインストールしておきます。
```
gu install native-image
```

using-kogito ディレクトリで以下のコマンドを実行します。ビルドには結構時間がかかります｡｡｡
```
./mvnw clean package -Dnative
```
これでバイナリイメージ target/using-kogito-1.0-SNAPSHOT-runner が生成されるので直接実行します。

```
./target/using-kogito-1.0-SNAPSHOT-runner
```

4ms で起動。爆速ですねー
```
2019-06-29 12:56:18,984 INFO  [io.quarkus] (main) Quarkus 0.18.0 started in 0.004s. Listening on: http://[::]:8080
```

さっきと同じ REST で動作します。
