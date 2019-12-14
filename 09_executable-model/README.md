# 09 Executable Model
この記事は [https://qiita.com/advent-calendar/2019/rh_engineers:title=赤帽エンジニア Advent Calendar 2019] 18 日目の記事です。

[https://tokobayashi.hatenablog.com/entry/2019/08/28/174641:title=07 MVEL] で書いたように、Drools 7 から Executable Model というオプションが利用可能となっています。これは kjar のビルド時点で DRL を Java クラス化し、実行時の性能を良くしよう、というものです。最新バージョンでは多数のバグがフィックスされ、安定して使えるようになってきました。

利用方法は簡単です。-DgenerateModel=YES というオプションを mvn に付けるだけです。

```
$ mvn clean install -DgenerateModel=YES
```

これで Executable Model を使用した kjar がビルドされます。 kjar の利用方法については [https://tokobayashi.hatenablog.com/entry/2019/06/27/144116:title=05 KJAR] を参照ください。

ではサンプルでやってみましょう。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 09_executable-model です。

kjar のプロジェクトはその中の drools-hello-kjar 、実行するテストクライアントのプロジェクトは drools-hello-client です。

drools-hello-kjar の Sample.drl の中には単純なルールが 1000 個入っています。

```
rule "rule0"
  when
    $p : Person( age >= 0 && age < 5 )
  then
    resultList.add( kcontext.getRule().getName() + " : " + $p );
end

rule "rule1"
  when
    $p : Person( age >= 5 && age < 10 )
  then
    resultList.add( kcontext.getRule().getName() + " : " + $p );
end
...
```

まずは普通にビルドしてみましょう。

```
$ cd drools-hello-kjar
$ mvn clean install
```

そしてテストします。こちらは 1000 個のファクトを投入して逐一 fireAllRules します。

```
$ cd drools-hello-client
$ mvn test
...
elapsed time for load  = 4828ms
elapsed time for execution = 1272ms
```

次は executable-model でビルドします。
```

$ cd drools-hello-kjar
$ mvn clean install -DgenerateModel=YES
```

"target/generated-sources" ディレクトリを覗いてみると分かりますが、大量の Java コードが生成されています。

```
$ tree target/generated-sources/
target/generated-sources/
└── drools-model-compiler
    └── main
        └── java
            ├── org
            │   └── example
            │       ├── LambdaPredicate00B17E09F368ECC48D2C9D8501927757.java
            │       ├── LambdaPredicate00BE0AF256D7A54ACF309C5375D52A02.java
            │       ├── LambdaPredicate00CD93B16506F18C96F2AF35D0A99C82.java
            │       ├── LambdaPredicate00E858E9381395E6402284C160581DD8.java
            │       ├── LambdaPredicate011DF5425A3D14FE82E6277403412AAF.java
            │       ├── LambdaPredicate012F668EF4CBE53CC643A6161D4CDFC9.java
            ...
```

もう一度テストしてみましょう。

```
$ cd drools-hello-client
$ mvn test
...
elapsed time for load  = 3979ms
elapsed time for execution = 632ms
```

速くなってますね！

ただし、あくまでこれは人工的なルールでの人工的なテストです。ルールのパフォーマンスは様々な条件が影響しますので、測定時には実際に使うルール、テストシナリオで計測するようにしてください。
