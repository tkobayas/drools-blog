# 03 推論
今回は Drools の重要な機能のひとつ、推論(Inference)を紹介します。推論というと少し難しく聞こえますが、「再評価」と言い換えてもよいです。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 03_inference です。

以下のルールを見てください。前回と同様のポイント計算ルールですが、前回より簡略化しています（ポイント率の話は抜き）。

```java
rule "春のキャンペーン"
	when
		$o : Order(consumer.memberCreatedAt >= "2019-04-01" && consumer.memberCreatedAt <=  "2019-04-30")
	then
		$o.setExtraPoint($o.getExtraPoint() + 2000);
		update($o);
end

rule "高額商品キャンペーン"
	when
		$o : Order(itemPrice > 100000)
	then
		$o.setExtraPoint($o.getExtraPoint() + 4000);
		update($o);
end

rule "大量ポイント獲得オーダー"
	when
		$o : Order(extraPoint > 5000)
	then
		$o.setSpecialPointOrder(true);
end
```

"update($o)" というのが今回の肝です。キャンペーンルールに該当し、ポイントが追加された場合、「update()」によりルールの再評価を行います。このとき「$o」つまり Order が更新されたよ、ということを伝えています。

最後のルール "大量ポイント獲得オーダー" は、"春のキャンペーン" と "高額商品キャンペーン" の両方に該当してポイントが 6000 になってはじめてマッチします。このようにルールの実行中に条件が変わってマッチするようなケースに「再評価」が必要になります。ファクトの更新「update()」以外にも挿入「insert()」や削除「delete()」も再評価のトリガーになります。

ここで「あれ？ "春のキャンペーン" や "高額商品キャンペーン" も再評価されて2重に実行されたりしないの？」と疑問に思った人もいるかも知れません。実は重要なポイントです。さっき「$o が更新されたよ、と伝える」と書きましたがこのときルールエンジンは $o のどのプロパティが変更されたかを意識します。つまり $o の extraPoint が変更された、と知っているので consumer や itemPrice は再評価しないのです。これは「Property Reactive」という機能で、Drools のバージョンによってデフォルトの動作が違う場合があります。このサンプルで使用している Drools 7.18.0.Final ではデフォルトで「Property Reactive」が有効です。

実行してみましょう。

```
$ cd 03_inference
$ mvn clean test
```

以下のようなアウトプットが出ます。

```
insert : Person [name=ジョン, memberCreatedAt=2019-04-11]
insert : Order [consumer=ジョン, itemName=ギター, itemPrice=200000]
実行 : 春のキャンペーン
実行 : 高額商品キャンペーン
実行 : 大量ポイント獲得オーダー
======================================
ポイントキャンペーンのご活用ありがとうございます!
======================================
```

ここで、"大量ポイント獲得オーダー" による specialPointOrder フラグはメッセージの表示にしか使っていませんが、アプリでの特別なオーダー処理や、更なるルールの判定につながるような使い方も考えられられます。

推論は強力な機能ですがユースケースによっては使う必要がないでしょう。使うときも自分が混乱しない程度に抑えて使うことをおすすめします。

