# 04 KJAR
前回まではルールをクラスパスに配置していました。アプリケーションにリソースを組み込むという意味では普通のやりかたです。一方 Drools ではルールをアプリケーションと分離して管理する方法も強力にサポートされています。

1. ルールを Maven を使って jar にまとめる。jar は Maven リポジトリにデプロイされる
2. アプリケーションは jar の GAV(GroupId, ArtifactId, Version) を指定して、Maven リポジトリから jar を読み込んでルールを使用する

この jar を KJAR (Knowledge JAR) と呼びます。

おっと、随分 Maven に依存してるように見えますね。上記のステップ 1 は kie-maven-plugin という Maven プラグインを利用して実現します。ステップ 2 では実際アプリケーションは内部的に Maven のライブラリを利用して動作します。開発者はそれほど意識しなくても大丈夫です（最終的な本番環境のアーキテクチャーなどは考慮が必要です。これはまた後日）。動かしてみましょう。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 05_kjar です。ディレクトリ "05_kjar" の中に2つのディレクトリがあります。

- drools-hello-kjar : こちらが KJAR のプロジェクト
- drools-hello-client : KJAR を利用するアプリケーション

```
$ cd 05_kjar
$ cd drools-hello-kjar
$ mvn clean install
```
pom.xml をちょっと見ておきましょう。

```xml
  <groupId>org.example</groupId>
  <artifactId>drools-hello-kjar</artifactId>
  <version>1.0.0</version>
  <packaging>kjar</packaging>
  ...
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>2.1</version>
        <configuration>
          <debug>true</debug>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.kie</groupId>
        <artifactId>kie-maven-plugin</artifactId>
        <version>${drools.version}</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
```

このように <packaging> を kjar を指定し、<build> に kie-maven-plugin を設定します。

また、 src/java/resources 下に META-INF/kmodules.xml が必要です。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<kmodule xmlns="http://jboss.org/kie/6.0.0/kmodule">
</kmodule>
```

ここに kbase や ksession に関する設定が書けますが、とりあえず上記のように空っぽでも大丈夫です。

生成される jar は基本的に普通の jar と変わりませんが、ルールのコンパイルなどを行い、エラーチェックをしてくれます。

これで KJAR が Maven ローカルリポジトリ (~/.m2/repository/) にインストールされます。ローカルリポジトリにあるので、次のクライアントコードは Maven 経由でこの jar を見つけることができます。

```
$ cd ../drools-hello-client
$ mvn clean test
```

```java
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.example", "drools-hello-kjar", "1.0.0");
        KieContainer kcontainer = ks.newKieContainer(releaseId);
        KieSession ksession = kcontainer.newKieSession();
```
このように、getKieClasspathContainer() の代わりに GAV を指定して KieContainer を生成します。後は同じです。

```
Hello Child, ポール
Hello Adult, ジョン
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.715 sec
```

KJAR をアプリケーションから分離することで、アプリケーションを変更/再デプロイせずにルールだけ更新する、という運用が可能になります。
