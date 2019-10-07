# 08 no-loop, lock-on-active
DRL には様々な属性(attribute)を指定することができます。

https://docs.jboss.org/drools/release/7.26.0.Final/drools-docs/html_single/#_rule_attributes

今回はそのなかで、no-loop と lock-on-active を紹介します。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 08_noloop_lockonactive です。

「03 推論」のときのルールをベースにしています。このとき私が

```
ここで「あれ？ "春のキャンペーン" や "高額商品キャンペーン" も再評価されて2重に実行されたりしないの？」と疑問に思った人もいるかも知れません。実は重要なポイントです。さっき「$o が更新されたよ、と伝える」と書きましたがこのときルールエンジンは $o のどのプロパティが変更されたかを意識します。つまり $o の extraPoint が変更された、と知っているので consumer や itemPrice は再評価しないのです。これは「Property Reactive」という機能で
```

と書いていたのを覚えているでしょうか。要するに Drools は変更されたプロパティと関係のないルールは再評価しない、ということです。しかし、「変更されたプロパティと関係あるルールだけど再評価して欲しくない」というパターンもありえます。今回のルールを見てください。

```java
rule "春のキャンペーン"
    //no-loop true
    //lock-on-active true
    when
        $o : Order(consumer.memberCreatedAt >= "2019-04-01" && consumer.memberCreatedAt <=  "2019-04-30", extraPoint < 20000)
    then
        System.out.println("実行 : " + kcontext.getRule().getName());
        $o.setExtraPoint($o.getExtraPoint() + 2000);
        update($o);
end

rule "高額商品キャンペーン"
    //no-loop true
    //lock-on-active true
    when
    $o : Order(itemPrice > 100000, extraPoint < 20000)
    then
        System.out.println("実行 : " + kcontext.getRule().getName());
        $o.setExtraPoint($o.getExtraPoint() + 4000);
        update($o);
end
```
2つのルールに「extraPoint < 20000」という条件が足されています。20000以上のエクストラポイントのオーダーはマッチしないようにしよう、ということです。さて、この場合ルールが実行されると extraPoint が更新されるのでルールが再度ヒットします。試してみましょう。

$ mvn test

```
insert : Person [name=ジョン, memberCreatedAt=2019-04-11]
insert : Order [consumer=ジョン, itemName=ギター, itemPrice=200000, specialPointOrder=false]
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 春のキャンペーン
実行 : 大量ポイント獲得オーダー
Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.842 sec <<< FAILURE!
testHello(org.example.DroolsTest)  Time elapsed: 1.79 sec  <<< FAILURE!
java.lang.AssertionError: expected:<3> but was:<11>
```

「春のキャンペーン」が繰り返し実行されてしまいましたね。。。ここでまず「no-loop」という属性があります。これは「自分自身を繰り返して実行しない」という属性です。2つのルールに「no-loop true」という行がコメントで書かれているので、コメントをはずして実行してみましょう。

```
insert : Person [name=ジョン, memberCreatedAt=2019-04-11]
insert : Order [consumer=ジョン, itemName=ギター, itemPrice=200000, specialPointOrder=false]
実行 : 春のキャンペーン
実行 : 高額商品キャンペーン
実行 : 春のキャンペーン
実行 : 高額商品キャンペーン
実行 : 春のキャンペーン
実行 : 高額商品キャンペーン
実行 : 春のキャンペーン
実行 : 大量ポイント獲得オーダー
Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.599 sec <<< FAILURE!
testHello(org.example.DroolsTest)  Time elapsed: 1.546 sec  <<< FAILURE!
java.lang.AssertionError: expected:<3> but was:<8>
```

残念！今度は「春のキャンペーン」と「高額商品キャンペーン」が交互に実行されています。「no-loop」は「自分自身の繰り返し」にしか効かないのです。無力、あまりに無力｡｡｡

このような場合は「lock-on-active」の出番です。「no-loop」は再度コメントアウトし、「lock-on-active true」の行のコメントをはずして実行してみましょう。

```
insert : Person [name=ジョン, memberCreatedAt=2019-04-11]
insert : Order [consumer=ジョン, itemName=ギター, itemPrice=200000, specialPointOrder=false]
実行 : 春のキャンペーン
実行 : 高額商品キャンペーン
実行 : 大量ポイント獲得オーダー
======================================
ポイントキャンペーンのご活用ありがとうございます!
======================================
----
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.533 sec
```

うまくいきましたね！「lock-on-active」は「一度呼ばれると二度と再実行されない（ruleflow-group/agenda-groupが変わらない限り）」です。こちらのほうが no-loop より求められていることが多いでしょう。

今日はここまで！
