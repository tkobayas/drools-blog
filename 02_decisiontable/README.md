# 02 デシジョンテーブル
前回は HelloWorld ということで基本の DRL でルールを書きましたが、次はよく使われる「デシジョンテーブル」を説明します。

デシジョンテーブル(意思決定表)は以下のように表形式で、条件と結果を表すものです。DRL よりずっと分かりやすいですね。

[image]

MS Excel や LibreOffice で書く XLS/XLSX 形式と、Drools のワークベンチ GUI (Decision Central)で書く GDST 形式がありますが、ここでは前者を説明します。

デシジョンテーブルのメリットは、Drools に詳しいメンバーが下準備さえ済ませれば、あとはビジネスユーザーがセルの値を埋めて行くだけ、という分業が簡単に行えることです。もちろん表にするまでもないルールは直接 DRL を書いて組み合わせることもあります。

実際のところ、デシジョンテーブルは DRL 生成テンプレートです。1行あたり、1ルールが内部的に生成されます。例えば上記の「ノーマルカード」の行は以下のような DRL になります。

```
rule "PointCalc_11"
	when
		$o : Order(consumer.card == MembershipCard.NORMAL)
	then
		$o.setPointRate($o.getPointRate().add(new BigDecimal("1.0")));
end
...
```

イメージついたでしょうか？

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 02_decisiontable です。お題は買い物した時のポイント付与サービスです。今回は4つのルールだけですが、実際にはものすごい量のルールだったりしますよね。

まずプロジェクト内のデシジョンテーブル

https://github.com/tkobayas/drools-blog/blob/master/02_decisiontable/src/main/resources/org/example/point-calc.xls

を見てみましょう。上記画像と同じものです。中身を順番に説明していきます。

まず前半は表共通の設定事項です。

- 2行目: RuleSet の次のセルはパッケージ名です
- 3行目: Import の次のセルは import するクラス名をカンマ区切りで並べます
- 4行目: Notes の次のセルはただの説明です。ルールには反映されません
- 6行目: RuleTable は半角スペース空けて、同じセルに表の名前を書きます。この名前が生成される各ルールのプリフィックスになります
- 7行目: 条件部は「CONDITION」、結果部は「ACTION」と記入します。DRLにおける 「when」「then」に相当します。必要に応じて何列でも追加できます。
- 8行目: 「CONDITION」の場合のみ、条件ファクトを指定します
- 9行目: 「CONDITION」の場合は、上記ファクトの制約条件を記入します。テンプレートの挿入変数がひとつの場合は「$param」、複数の場合は「$1」「$2」...と書きます。「ACTION」の場合は「then」で実行したい Java コードを書きます。同様にテンプレートの挿入変数が使えます。

後半が実際にテンプレートに挿入する値となります。

- 10行目: コメント用。以下のセルに何を入れればよいのかわかりやすく書きましょう。
- 11行目: ここ以降が1行あたり1ルールに相当します。セルの値が上記9行目のテンプレートに適用されます。複数変数の場合はカンマ区切りです。空欄の場合はその列は丸ごと使用されません。B列も単なるコメントです。
- 12行目: 以下同様

次に Java コードを見てみましょう。

https://github.com/tkobayas/drools-blog/blob/master/02_decisiontable/src/test/java/org/example/DroolsTest.java

```java
        // デフォルトの dateformat は "dd-MMM-yyyy" (例: "01-Apr-2019") なので変更する
       System.setProperty("drools.dateformat", "yyyy-MM-dd");
```
まず日付フォーマットは日本向け ("yyyy-MM-dd") に変更しておきます。ソースコメントのとおりです。

```java
        SpreadsheetCompiler compiler = new SpreadsheetCompiler();
        String drl = compiler.compile(ks.getResources().newClassPathResource("org/example/point-calc.xls").getInputStream(), InputType.XLS);
        System.out.println(drl);
```

これはデバッグ用コードで、本当は省いても構いません。このコードにより、デシジョンテーブルから変換された DRL を見る事ができます。ルールのコンパイルエラーなどがあったときには重宝します。

```java
        KieContainer kcontainer = ks.getKieClasspathContainer();
        KieSession ksession = kcontainer.newKieSession();
        ...
        ksession.insert(john);
        ...
        ksession.insert(order);
        ...
        int fired = ksession.fireAllRules();
```

あとは HelloWorld と同じですね。

さて、実際に動かしてみましょう。

ジョンが20万円のギターを買いました。メンバーズカードはシルバーなので 2% 還元。また、この4月に入会したので「春の特別キャンペーン」が適用され、還元率 0.5 アップの 2.5%。更に特別ポイントが 1000 ポイント加算されます。

```
$ cd 02_decisiontable
$ mvn clean test

...

+++ ルール実行開始 +++
insert : Person [name=ジョン, memberCreatedAt=2019-04-11, card=SILVER]
insert : Order [consumer=ジョン, itemName=ギター, itemPrice=200000]
======================================
お買い上げにより、 6000 ポイントが付与されます
```
200000*0.025+1000=6000ですね。
