# 09 Executable Model
この記事は [https://qiita.com/advent-calendar/2019/rh_engineers:title=赤帽エンジニア Advent Calendar 2019] の 12/18 分の記事です。

[https://tokobayashi.hatenablog.com/entry/2019/08/28/174641:title=07 MVEL] で書いたように、Drools 7 から Executable Model というオプションが利用可能となっています。これは kjar のビルド時点で DRL を Java クラス化し、実行時の性能を良くしよう、というものです。最新バージョンでは多数のバグがフィックスされ、安定して使えるようになってきました。

利用方法は簡単です。-DgenerateModel=YES というオプションを mvn に付けるだけです。

```
$ mvn clean install -DgenerateModel=YES
```
これで Executable Model を使用した kjar がビルドされます。 kjar の利用方法については [https://tokobayashi.hatenablog.com/entry/2019/06/27/144116:title=05 KJAR] を参照ください。

