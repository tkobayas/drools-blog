# 04 KJAR
前回まではルールをクラスパスに配置していました。アプリケーションにリソースを組み込むという意味では普通です。一方 Drools ではルールをアプリケーションと分離して管理する方法も強力にサポートされています。

1. ルールを Maven を使って jar にまとめる。jar は Maven リポジトリにデプロイされる
2. アプリケーションは jar の GAV(GroupId, ArtifactId, Version) を指定して、Maven リポジトリから jar を読み込んでルールを使用する

この jar を KJAR (Knowledge JAR) と呼びます。

おっと、随分 Maven に依存してるように見えますね。上記のステップ 1 は kie-maven-plugin という Maven プラグインを利用して実現します。ステップ 2 では実際アプリケーションは Maven のライブラリを利用して動作します。開発者はそれほど意識しなくても大丈夫です（最終的な本番環境のアーキテクチャーなどは考慮が必要です。これはまた後日）。動かしてみましょう。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 04_kjar です。ディレクトリ "04_kjar" の中に2つのディレクトリがあります。

- drools-hello-kjar : こちらが KJAR のプロジェクト
- drools-hello-client : KJAR を利用するアプリケーション

```
$ cd  04_kjar
$ cd drools-hello-kjar
$ mvn clean install
```

これで KJAR が Maven ローカルリポジトリ (~/.m2/repository/) にインストールされます。pom.xml をちょっと見ておきましょう。

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