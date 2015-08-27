sample-boot-hibernate
---

## はじめに

Spring Boot / Spring Security / Hibernate ORM を元にしたDDDサンプル実装です。  
ベーシックな基盤は[ddd-java](https://github.com/jkazama/ddd-java)から流用しています。

考え方の骨子については以前発表した資料([Spring Bootを用いたドメイン駆動設計](http://www.slideshare.net/jkazama/jsug-20141127))を参照してください。

> 動作検証含めてまだ実装中です。

---

本サンプルでは[Spring Boot](http://projects.spring.io/spring-boot/)と[Lombok](http://projectlombok.org/)、[Hibernate ORM](http://hibernate.org/orm/) を利用してドメインモデリングの実装例を示します。実際に2007年くらいから現在に至るまで現場で利用されている実装アプローチなので、参考例の一つとしてみてもらえればと思います。  
※JavaDocに記載をしていますが、サンプルに特化させているので実際の製品コードが含まれているわけではありません。

### レイヤリングの考え方

オーソドックスな三層モデルですが、横断的な解釈としてインフラ層を考えます。

- UI層 - ユースケース処理を公開(必要に応じてリモーティングや外部サイトを連携)
- アプリケーション層 - ユースケース処理を集約(外部リソースアクセスも含む)
- ドメイン層 - 純粋なドメイン処理(外部リソースに依存しない)
- インフラ層 - DIコンテナやORM、各種ライブラリ、メッセージリソースの提供

UI層の公開処理は通常JSPやThymeleafを用いて行いますが、本サンプルでは異なる種類のクライアント利用を想定してRESTfulAPIでの公開を前提とします。(API利用前提のサーバ解釈)

### Spring Boot の利用方針

Spring Boot は様々な利用方法が可能ですが、本サンプルでは以下のポリシーを用います。

- 設定ファイルはymlを用いる。Bean定義にxml等の拡張ファイルは用いない。
- ライブラリ化しないので@Beanによる将来拡張性を考慮せずにクラス単位でBeanベタ登録。
- 例外処理は終端(RestErrorAdvice/RestErrorCotroller)で定義。whitelabel機能は無効化。
- ORM実装としてHibernateに特化。(記法はJPA準拠)
- Spring Security の認証方式は昔からよくあるHttpSessionを利用するアプローチ。

### Javaコーディング方針

Java8以上を前提としていますが、従来のJavaで推奨される記法と異なっている観点も多いです。  
以下は保守性を意識した上で簡潔さを重視した方針となっています。

- Lombokを積極的に利用して冗長さを排除
- 名称も既存クラスと重複しても良いのでなるべく簡潔に
- インターフェースの濫用をしない
- ドメインの一部となるDTOなどは内部クラスで表現

> 現在 Java7 -> 8 へのリファクタ途中

### パッケージ構成

パッケージ/リソース構成については以下を参照してください。

```
main
  java
    sample
      context                         … インフラ層
      controller                      … UI層
      model                           … ドメイン層
      usecase                         … アプリケーション層
      util                            … 汎用ユーティリティ
      - Application.java              … 実行可能な起動クラス
  resources
    - application.yml                 … 設定ファイル
    - ehcache.xml                     … Spring Cache 設定ファイル
    - logback.xml                     … ロギング設定ファイル
    - messages-validation.properties  … 例外メッセージリソース
    - messages.properties             … メッセージリソース
```

## サンプルユースケース

サンプルユースケースとしては以下を想定します。

- **口座残高100万円を持つ顧客**が出金依頼(発生 T, 受渡 T + 3)をする。
- **システム**が営業日を進める。
- **システム**が出金依頼を確定する。(確定させるまでは依頼取消行為を許容)
- **システム**が受渡日を迎えた入出金キャッシュフローを口座残高へ反映する。

## 動作確認

サンプルはGradleを利用しているので、IDEやコンソールで動作確認を行うことができます。

### Eclipse

開発IDEである[Eclipse](https://eclipse.org/)で本サンプルを利用するには、事前に以下の手順を行っておく必要があります。

- JDK8以上のインストール
- [Lombok](http://projectlombok.org/download.html)のパッチ当て(.jarを実行してインストーラの指示通りに実行)
- Gradle Plugin (Pivotal) のインストール

次の手順で本サンプルをプロジェクト化してください。  

1. パッケージエクスプローラから「右クリック -> Import -> Project」で*Gradle Project*を選択して*Next*を押下
1. *Root folder:*にダウンロードした*sample-boot-hibernate*ディレクトリを指定して*Build Model*を押下
1. *Project*で*sample-boot-hibernate*を選択後、*Finish*を押下(依存ライブラリダウンロードがここで行われます)

次の手順で本サンプルを実行してください。

1. *Application.java*に対し「右クリック -> Run As -> Java Application」
1. *Console*タブに「Started Application」という文字列が出力されればポート8080で起動が完了


### コンソール

Windows/Macのコンソールから実行するにはGradleのコンソールコマンドで行います。  
※事前にJDK8以上のインストールが必要です。

1. ダウンロードした*sample-boot-hibernate*ディレクトリ直下へコンソールで移動
1. 「gradlew bootRun」を実行
1. コンソールに「Started Application」という文字列が出力されればポート8080で起動が完了

### 動作確認

Eclipseまたはコンソールでサーバを立ち上げた後、testパッケージ配下にある`SampleClient`の各検証メソッドをユニットテストで実行してください。

顧客向けユースケース

- /api/asset/cio/withdraw  
振込出金依頼 [accountId: sample, currency: JPY, absAmount: 出金依頼金額]
- /api/asset/cio/unprocessedOut  
振込出金依頼未処理検索

社内向けユースケース

- /api/admin/asset/cio  
振込入出金依頼検索 [updFromDay: 更新From(yyyyMMdd), updToDay: 更新To(yyyyMMdd)]

バッチ向けユースケース

- /api/system/job/daily/processDay  
営業日を進める(単純日回しのみ)
- /api/system/job/daily/closingCashOut  
当営業日の出金依頼を締める
- /api/system/job/daily/realizeCashflow  
入出金キャッシュフローを実現する(受渡日に残高へ反映)

## 本サンプルを元にしたプロジェクトの作成

本プロジェクトを元にしてプロジェクトベースを作成したい場合は以下の手順を実行してください。

1. build.gradle 内の拡張タスク(copyProject)の上部に定義されている変数を修正
1. コンソールから「gradlew copyProject」を実行

以降のプロジェクトインポート手順は前述のものと同様となります。

## License

本サンプルのライセンスはコード含めて全て*MIT License*です。  
Spring Bootを用いたプロジェクトの立ち上げ時に実装サンプルとして利用してください。
