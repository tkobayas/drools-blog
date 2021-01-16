# 13 DMN
DMN (Decision Model and Notation) とは OMG (Object Management Group) で策定されたビジネスルールについての仕様です。

https://www.omg.org/dmn/

ビジネスプロセス仕様 BPMN (Business Process Model and Notation) のルール版だと思ってよいでしょう。

さて、DMN と、この Drools ブログでここまでやってきた DRL とはどう関係するのでしょう？ 答えは｡｡｡別物です！ 文法も違いますし、実行エンジンも違います。

ですので DRL をやってきた人にとっては新しい記述法を覚えなければいけないわけで、どのようなメリットがあるかが最初の関心事でしょう。初めて Drools を使う人であれば DRL か DMN のどっちを使えばいいのか？という疑問が出ますね。では以下に簡単に説明します。

DMN のメリット
* ルールがビジュアル化される
* 標準仕様である

DMN のデメリット (DRL と比較した場合)
* ステートレス実行のみ
* 推論(Inference)が使えない [https://tokobayashi.hatenablog.com/entry/2019/05/06/160337]

といったところです。

個人的には「ルールがビジュアル化される」が非常に大きいと考えられます。BPMN の場合、ビジネスプロセスがビジュアル化されるわけですが、これはどの開発プロジェクトでも使用ツールはともかく(パワポやVisioなど)、「業務プロセス」のビジュアル化はされていることでしょう。でも業務ルールのビジュアル化ってあんまりやってないのでは？詳細設計書に文章でずらずら書かれているとか。。。DMN で概要をいったんビジュアル化するだけでも新しい地平が開けるのではないでしょうか。

では実際に使ってみましょう。サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 13_DMN です。お題はドライバーが交通違反した時の罰金＆免停です。

プロジェクトの中身がめっちゃ少ない事に気付くでしょうか。実質 DMN を定義する 

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
