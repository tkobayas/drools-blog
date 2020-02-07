# 10 Business Central
Drools には Business Central という強力な Web UI ツールが付いています。以下、機能を列挙しますと

- ルール、デシジョンテーブル、データオブジェクト、プロセスなど、ファイルの管理
- プロジェクトの管理
- ルール、デシジョンテーブル、データオブジェクト、プロセスなどのエディター
- テストシナリオの作成、実行
- プロジェクトから kjar をビルド、kie-server（後日解説）へのデプロイ
- kie-server インスタンスの管理
- ロールベースのアクセス制限
- プロセス、タスクの実行(jBPM)
- プロセスインスタンスの監視(jBPM)

などなど。

ファイルは Git でバージョン管理されており、他の Git クライアントとの連携ももちろん出来ます。

ちなみに Workbench, Decision Central などの呼び名もありますが、「Business Central」で統一する方向のようです。

では使ってみましょう。

こちらのリンクの「Business Central Workbench」から「WildFly 14 WAR」をダウンロードします。

[https://www.drools.org/download/download.html]

WildFly 本体はこちらからダウンロードします。（EAP でも OK です）

[https://wildfly.org/downloads/]

最新の WildFly 18.0.1.Final でも動作しましたが、ちょっとエラーが出るようです。今回は 14.0.1.Final でやります。「14.0.1.Final」の「Java EE Full & Web Distribution」をダウンロードします。

wildfly-14.0.1.Final.zip を解凍したら wildfly-14.0.1.Final/standalone/deployments の下に先ほどダウンロードした business-central-7.32.0.Final-wildfly14.war コピーします。

wildfly-14.0.1.Final/bin に移動し、add-user.sh でユーザを作成します (Windows の人は適宜 bat に読み替えて下さい) b) Application User の方です。ユーザ名は何でも構いません。ロールは admin,kie-server,rest-all を与えます。
```
$ ./add-user.sh 

What type of user do you wish to add? 
 a) Management User (mgmt-users.properties) 
 b) Application User (application-users.properties)
(a): b

Enter the details of the new user to add.
Using realm 'ApplicationRealm' as discovered from the existing property files.
Username : rhdmAdmin
...
Password : 
Re-enter Password : 
What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]: admin,kie-server,rest-all
About to add user 'rhdmAdmin' for realm 'ApplicationRealm'
Is this correct yes/no? yes
...
yes/no? yes
...
```

また WildFly デフォルトの -Xmx と -XX:MaxMetaspaceSize は少ないのでそれぞれ 2G くらいに増やしておきます。

bin/standalone.conf
```
   JAVA_OPTS="-Xms64m -Xmx2g -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=2g -Djava.net.preferIPv4Stack=true"
```

では WildFly を起動します。

```
$ ./standalone.sh
```

デプロイには少々時間がかかります。。。

```
...
16:36:52,739 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 42) WFLYSRV0010: Deployed "business-central-7.32.0.Final-wildfly14.war" (runtime-name : "business-central-7.32.0.Final-wildfly14.war")
```

以下の URL でアクセスします。
```
http://localhost:8080/business-central-7.32.0.Final-wildfly14
```

先ほど作ったユーザでログインします。
[f:id:tokobayashi:20200207155133p:plain]

まず最初に日本語UIに変更しておきましょう。右上の歯車アイコンをクリックし、Settings メニューから「Languages」をクリック、セレクトボックスから「Japanese」を選んで OK をクリックしてください。
[f:id:tokobayashi:20200207155153p:plain]

日本語になりましたね！

ではプロジェクトを作っていきましょう。 [メニュー]->[プロジェクト] をクリックすると「スペース」という画面になります。スペースというのは複数のプロジェクトをまとめる単位です。デフォルトで「MySpace」というスペースがあるのでそれを使います。「MySpace」をクリックすると｡｡｡
[f:id:tokobayashi:20200207155210p:plain]

どーんと「ここには何もありません」と表示されますね。「プロジェクトの追加」からプロジェクトを作成します。ちなみに「サンプルを試す」で様々なサンプルを見ることができます。ルール関連だと「Mortgages」サンプルがおすすめです。

「プロジェクトの追加」をクリックするとポップアップが出るのでプロジェクト名を決めます。ここでは helloProject とします。プロジェクトが作成されてもまだ空っぽなので「ここには何もありません」と表示されています。

データオブジェクト（= POJO）を作ってみましょう。「アセットの追加」をクリックします。様々なアセットを作成できます。「データオブジェクト」をクリックするとポップアップが出ます。データオブジェクト名を Person 、パッケージを com.myspace.helloproject とし、OK をクリックします。
データオブジェクトを作ってみましょう。「アセットの追加」をクリックします。様々なアセットを作成できます。「データオブジェクト」をクリックするとポップアップが出ます。データオブジェクト名を Person 、パッケージを com.myspace.helloproject とし、OK をクリックします。

この画面でデータオブジェクトのフィールドを追加できます。「フィールドを追加」から

- name を String
- age を int
- adult を boolean

 ｡｡｡ と追加していきます。最後に「保存」を忘れずに！

「ソース」タブをクリックすればわかるように、これは単純に Java ソースを作ってくれるものです。この画面でデータオブジェクトを作成する以外にも、別途必要なクラスを持った jar ファイルを dependency に設定することでそのクラスをルールで使えます。
[f:id:tokobayashi:20200207155236p:plain]

続いてルールを書きます。

「アセットの追加」（プロジェクトが空っぽじゃないときは右上にボタンがあるよ！）をクリックして「ガイド付きルール」を選択します。ルール名は helloPerson 、パッケージは com.myspace.helloproject とします。「ガイド付きルール」というのは GUI でルールを書くというものです。右端の方にあるプラスアイコンをクリックして WHEN や THEN の中身を追加していきます。まず WHEN のプラスアイコンをクリック、「条件をルールに追加...」から「Person」を選択します。するとルールに「Person があります」という文が追加されます。そこから詳細を設定していきます。

- 「Person があります」をクリック、「フィールドに制限を追加」 -> age を選択
- age の右のセレクトボックスから「は次の値よりも大きい」を選択
- その右のえんぴつアイコンをクリック -> 「固定値」をクリック
- テキストボックスに 20 を入力
- 「Person があります」をクリック、「変数名」のテキストボックスに $p を入力し「設定」をクリック

続いて THEN のプラスアイコンをクリック、「$p のフィールド値を変更」を選択、「Person [$p] の値 設定」という文が追加されるのでそれをクリックし、セレクトボックスから adult を追加し、値に true を選択します。最後に「保存」。
[f:id:tokobayashi:20200207155252p:plain]

「ソース」タブを見ればわかるように、これは DRL を作成してくれます。

続いてテストシナリオを書きます。「アセットの追加」->「テストシナリオ」、名前は testscenario1 、パッケージは com.myspace.helloproject とします。

- 「GIVEN」列の「INSTANCE1」をクリック、右の「テストツール」パネルから「Person」を選択、「Insert Data Object」をクリック
- すると「INSTANCE1」が「Person」に変化します。次はその下のセルをクリックし、「テストツール」パネルから name を選択、「Insert Data Object」をクリック
- するとそのセルが「name」に変化。そのセルを右クリックし、「右に列を挿入」を選択
- すると右にセルが増えるので、同様に「テストツール」パネルから age を選択、「Insert Data Object」をクリック
- 「EXPECT」列も同様に INSTANCE を「Person」に、PROPERTY は「adult」を指定（列は増やさない）
- 1行目は name「太郎」、age「35」、adult「true」で、値を入力
- 右クリック「行を下に挿入」で行を増やせるので2行目は name「次郎」、age「18」、adult「false」にする
- 保存
- メニュー中央の三角（再生）ボタンをクリック！右側に結果が出ます。テストシナリオでルールが正しく動いていることを検証できました。

[f:id:tokobayashi:20200207155308p:plain]

Business Central の機能はまだまだたくさんありますが、今回はここまで！