# 01 Drools HelloWorld
こんにちは、これから Drools についていろいろと書いていこうと思います。サンプルと共に、分かりやすい入門的なエントリを中心にしていく予定です。

Drools というのはオープンソースのルールエンジンで、Java で書かれています。

https://www.drools.org/

ルールエンジンとは、簡単に言うとインプットをルールに基づいて処理してアウトプットする、というエンジンです。あ、普通のプログラミングと同じですね？そうです、では普通の手続き型プログラミング言語（例えば Java）とどう違うかというと、ルールは宣言的(Declarative)に書かれる、ということです。例えばこのルールを見てください。

```java
rule "Hello Child"
    when
        $p : Person( age < 12 )
    then
        // 何かする
end

rule "Hello Adult"
    when
        $p : Person( age >= 20 )
    then
        // 何かする
end
```

1つ目のルールは「12歳未満なら XXX する」

2つ目のルールは「20歳以上なら YYY する」

というものです。Java であれば

```java
if (person.getAge() < 12) {
    // 何かする
}
if (person.getAge() >= 20) {
    // 何かする
}
```

のようになるでしょう。一見たいして変わらないようですが、大きな違いがあります。Java の場合は「書かれた順番通りに処理される」のです。数千、数万の複雑なルールの場合、全てを上から順番に処理するのは現実的ではないでしょう。プログラマは最適化のために、様々な手を打てます（データによって不要な部分を評価しない、共通部分をまとめて評価、キャッシュ/インデックス化など）。しかしそれをするとおそらくルールは最適化ロジックと混じり合い、ビジネスユーザが解読できないものになるでしょう。また、その後のルール追加、変更も困難になります。

ルールエンジンの場合、最適化はエンジンがやってくれます。ユーザは単純にルールを並列に並べるだけです。(もちろん「順番」がルール上必要な場合はいくつかの手段、文法があります。それはまた後日)

さて、概要はこのくらいにして早速 HelloWorld しましょう。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 01_helloworld です。

ではソースコードを見てみましょう。

まずルールです。

https://github.com/tkobayas/drools-blog/blob/master/01_helloworld/src/main/resources/org/example/Sample.drl
```java
package org.example
 
import org.example.Person;

rule "Hello Child"
    when
        $p : Person( age < 12 )
    then
        System.out.println( "Hello Child, " + $p.getName());
end

rule "Hello Adult"
    when
        $p : Person( age >= 20 )
    then
        System.out.println( "Hello Adult, " + $p.getName());
end
```
これは DRL (Drools Rule Language) という言語で書かれています。新しい言語を覚えるというと若干ハードルが高いですが、結構簡単です。見た通り、"when"に条件を書き、"then"に結果を書くだけです。"then"のところは普通の Java で書けます。"$p" はバインド変数です。マッチした Person を "then" で参照できます。

ではこのルールをどう実行するかというと、こちらの Java コードを見てください。

https://github.com/tkobayas/drools-blog/blob/master/01_helloworld/src/test/java/org/example/DroolsTest.java

```java
        KieServices ks = KieServices.Factory.get();
        KieContainer kcontainer = ks.getKieClasspathContainer();
        KieSession ksession = kcontainer.newKieSession();
```

この3行は定型処理と思ってください。クラスパスにルールを置いた場合はこうなりますが、別の方法も後日紹介します。KieSession が、ユーザが使うエンジンインターフェースです。ksession と略されることもあります。

```java
        Person john = new Person("ジョン", 25);
        ksession.insert(john);
        Person paul = new Person("ポール", 10);
        ksession.insert(paul);
```

2つの Person オブジェクトを ksession に insert します。Person はただの POJO です。ファクト(Fact) と呼ばれることもあります。

```java
        int fired = ksession.fireAllRules();
        assertEquals(2, fired);
```
fireAllRules() でマッチしたルールが実行(fire)されます。戻り値は実行されたルールの数です。

```java
        ksession.dispose();
```

最後、dispose() は忘れないように呼んでください。ksession に紐づいたリソースを解放します。

では maven で実行してみましょう。

```
$ cd 01_helloworld
$ mvn clean test
```

アウトプットはこんな感じになるでしょう

```
...
Running org.example.DroolsTest
...
Hello Child, ポール
Hello Adult, ジョン
...
```

ルール通りに出力されましたね。

まずはここまで！