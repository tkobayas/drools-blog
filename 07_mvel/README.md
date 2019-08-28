# 07 MVEL
MVEL というのは Java ベースの言語で、 Java をより簡単に表記する事を目的にしています。超ざっくり言うと、getter/setter を省略して書いたりできます。

```java
user.name == 'ジョン レノン'
```

ドキュメントはこちらを参照ください。

http://mvel.documentnode.com/

Drools ではルールを簡便に書くために MVEL を内部的に利用しています。大きく分けると 3つの箇所で、MVEL が使用されます。

1. ルール制約(Constraint)

例えば

```java
        $p : Person( age >= 26 )
```

の age >= 28 の部分は *はじめは* MVEL によって処理されます。よって先程書いた getter の省略などが可能です。

今 *はじめは* って書きましたね。実はこの制約部分は20回実行されると動的に Java クラスが生成、コンパイルされ、以降は MVEL ではなく Java クラスとして実行されます。JVM の JIT と同じような考え方で、よく実行される部分は最適化してパフォーマンスを上げようというものです。

ただ「いや、そもそも最初から Java クラス生成すればいいんじゃね？」という意見から、もうその方向に開発が進んでいます。これは executable-model というオプションで、すでに Drools 7 では利用可能です。おそらくバージョン 8 ではデフォルトの動作になるでしょう。

いずれにせよユーザにとって内部的な挙動は気にしなくてもよいのですが、「しばらく実行したあとに突然ルールの挙動が変わった」ような問題が発生した場合は、動的な Java クラス生成時のバグが原因だった、なんてこともあります。

2. eval

以下のように eval という文法が使えます。

```java
when
  p1 : Parameter()
  p2 : Parameter()
  eval( p1.getList().containsKey( p2.getItem() ) )
```

これは eval() 内部をまるごと MVEL で解釈し、true が返ればルールにマッチする、というものです。Java 的なロジックをゴリゴリ書けるので、一部で好まれるのですが、Drools エンジンからは最適化ができないのでパフォーマンス上おすすめしません。極力普通のフィールド制約で記述するのが Drools のパフォーマンスを上げるコツです。また、eval は近いうちに deprecated になるのではないかと言われています。

3. RHS

ルールの RHS (then の部分です)を MVEL を使って表記できます。その場合、

```java
  dialect "mvel"
```
を宣言する必要があります。dialect は package 単位もしくは rule 単位で宣言できます。"mvel" 以外には "java" があります。デフォルトは "java" です。

ここで MVEL を使うメリットは｡｡｡ BigDecimal の四則演算が簡単だからです。ていうかそれしか理由が無いのではないかと思われます。(あと getter/setter が無いと日本語フィールド名が見やすいとか)

```java
    then
        $p.salary = $p.salary + 50000;
end
```
ただここまで説明しておいてなんですが、Drools チームは MVEL への依存を無くす方向へ進んでいます。ですので、新規開発では、できれば 2 と 3 の利用方法は避けていただいたほうがよいのではないかと思います。(1 は executable-model で内部的に変更される)

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 07_mvel です。

このようなルールです。eval は使っていません。

https://github.com/tkobayas/drools-blog/blob/master/07_mvel/src/main/resources/org/example/Sample.drl
```java
package org.example
 
import org.example.Person;

dialect "mvel"

rule "昇給"
    when
        $p : Person( age >= 26 )
    then
        $p.salary = $p.salary + 50000;
end
```
実行すると

```
$ mvn clean test

...
Running org.example.DroolsTest
...
ジョン の給料は 350000円です。
...
```

ルール通りに出力されましたね。
