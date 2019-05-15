# 04 ステートレス ksession
ここまで、普通の ksession つまり ステートフル ksession を使ってきましたが、今回は ステートレス ksession の使い方です。

ステートレス ksession とは、「ステートフル ksession のラッパーで、insert, fireAllrules, dispose をまとめてやってくれる便利クラス」です。「まとめてやって、終わってくれる」ので、ユーザが ksession の状態（ステート）を気にする必要はありません。

サンプルではステートフル ksession を使うことが多いのですが、実際のユースケースでは ステートレス ksession で十分な場合が多く、またそのほうがコードも簡潔になるのでおすすめです。

サンプルコードはこちらから clone してください。

```
git clone https://github.com/tkobayas/drools-blog.git
```

今日のエントリはその中の 04_stateless です。

ルールは前回の 03 推論(Inference) と全く同じです。Java コードの以下の部分だけが違っています。

```java
        StatelessKieSession ksession = kcontainer.newStatelessKieSession();
```
newKieSession() のかわりに newStatelessKieSession() で ksession を取得します。

```java
        Command insertElementsCommand = CommandFactory.newInsertElements(Arrays.asList(john, order));
        List<InternalFactHandle> factHandleList = (List<InternalFactHandle>) ksession.execute(insertElementsCommand);
```

Insert したいファクトを List にして newInsertElements() で Command を作成します。その Command を ksession.execute() に渡せばOKです。

Insert だけしか指示していないようですが、最後に自動的に fireAllRules() を行い、さらに dispose() もやってくれます。

mvn test の結果は  03 推論(Inference) のときと同じであることが確認できると思います。

ステートレス ksession は上記のような、InsertElementsCommand 以外にも様々な Command を実行できます。複数の Command をまとめて BatchExecutionCommand にして実行させることもできます。いずれにせよ ksession.execute() で全てが実行され、終了します。
