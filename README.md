sample-boot-hibernate
---

### はじめに

[Spring Boot](http://projects.spring.io/spring-boot/) / [Spring Security](http://projects.spring.io/spring-security/) / [Hibernate ORM](http://hibernate.org/orm/) を元にしたDDDサンプル実装です。  
ベーシックな基盤は[ddd-java](https://github.com/jkazama/ddd-java)から流用しています。  
フレームワークではないので、Spring Bootを利用するプロジェクトを立ち上げる際に元テンプレートとして利用して下さい。

考え方の骨子については以前発表した資料([Spring Bootを用いたドメイン駆動設計](http://www.slideshare.net/jkazama/jsug-20141127))を参照してください。

---

本サンプルはSpring Bootを用いたドメインモデリングの実装例としても利用できます。実際にそれなりの規模の案件で運用されていた実装アプローチなので、モデリングする際の参考事例としてみてもらえればと思います。  

*※ JavaDocに記載をしていますが参考実装レベルです。製品水準のコードが含まれているわけではありません。*

#### レイヤリングの考え方

オーソドックスな三層モデルですが、横断的な解釈としてインフラ層を考えています。

| レイヤ          | 特徴                                                        |
| -------------- | ----------------------------------------------------------- |
| UI             | ユースケース処理を公開(必要に応じてリモーティングや外部サイトを連携) |
| アプリケーション | ユースケース処理を集約(外部リソースアクセスも含む)                 |
| ドメイン        | 純粋なドメイン処理(外部リソースに依存しない)                      |
| インフラ        | DIコンテナやORM、各種ライブラリ、メッセージリソースの提供          |

UI層の公開処理は通常JSPやThymeleafを用いて行いますが、本サンプルでは異なる種類のクライアント利用を想定してRESTfulAPIでのAPI提供のみをおこないます。(利用クライアントは別途用意する必要があります)

#### Spring Boot の利用方針

Spring Boot は様々な利用方法が可能ですが、本サンプルでは以下のポリシーで利用します。

- 設定ファイルはymlを用いる。Bean定義にxml等の拡張ファイルは用いない。
- ライブラリ化しないので@Beanによる将来拡張性を考慮せずにクラス単位でBeanベタ登録。
- 例外処理は終端(RestErrorAdvice/RestErrorCotroller)で定義。whitelabel機能は無効化。
- ORM実装としてHibernateに特化。
- Spring Security の認証方式はベーシック認証でなく、昔からよくあるHttpSessionで。
- 基礎的なユーティリティでSpringがサポートしていないのは簡易な実装を用意。

#### Javaコーディング方針

Java8以上を前提としていますが、従来のJavaで推奨される記法と異なっている観点も多いです。  
以下は保守性を意識した上で簡潔さを重視した方針となっています。

- Lombokを積極的に利用して冗長さを排除。
- 名称も既存クラスと重複しても良いのでなるべく簡潔に。
- インターフェースの濫用をしない。
- ドメインの一部となるDTOなどは内部クラスで表現。
- Java8で追加された概念/記法は積極的に利用。

#### パッケージ構成

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

### サンプルユースケース

サンプルユースケースとしては以下のようなシンプルな流れを想定します。

- **口座残高100万円を持つ顧客**が出金依頼(発生 T, 受渡 T + 3)をする。
- **システム**が営業日を進める。
- **システム**が出金依頼を確定する。(確定させるまでは依頼取消行為を許容)
- **システム**が受渡日を迎えた入出金キャッシュフローを口座残高へ反映する。

### 動作確認

本サンプルは[Gradle](https://gradle.org/)を利用しているので、IDEやコンソールで手間なく動作確認を行うことができます。

*※ライブラリダウンロードなどが自動で行われるため、インターネット接続が可能な端末で実行してください。*

#### サーバ起動（Eclipse）

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
1. ブラウザを立ち上げて「http://localhost:8080/api/management/health」で状態を確認

#### サーバ起動（コンソール）

Windows/Macのコンソールから実行するにはGradleのコンソールコマンドで行います。  

*※事前にJDK8以上のインストールが必要です。*

1. ダウンロードした*sample-boot-hibernate*ディレクトリ直下へコンソールで移動
1. 「gradlew bootRun」を実行
1. コンソールに「Started Application」という文字列が出力されればポート8080で起動が完了
1. ブラウザを立ち上げて「http://localhost:8080/api/management/health」で状態を確認

#### クライアント検証

Eclipseまたはコンソールでサーバを立ち上げた後、testパッケージ配下にある`SampleClient`の各検証メソッドをユニットテストで実行してください。

##### 顧客向けユースケース

| URL                              | 処理                 | 実行引数 |
| -------------------------------- | ------------------- | ------------- |
| `/api/asset/cio/withdraw`        | 振込出金依頼          | [`accountId`: sample, `currency`: JPY, `absAmount`: 依頼金額] |
| `/api/asset/cio/unprocessedOut/` | 振込出金依頼未処理検索 | -       |

*※振込出金依頼はPOST、それ以外はGET*

##### 社内向けユースケース

| URL                     | 処理             | 実行引数                                           |
| ----------------------- | --------------- | ------------------------------------------------- |
| `/api/admin/asset/cio/` | 振込入出金依頼検索 | [`updFromDay`: yyyy-MM-dd, `updToDay`: yyyy-MM-dd]|

*※GET*

##### バッチ向けユースケース

| URL                                     | 処理                                          | 実行引数 |
| --------------------------------------- | --------------------------------------------- | ------ |
| `/api/system/job/daily/processDay`      | 営業日を進める(単純日回しのみ)                    | -      |
| `/api/system/job/daily/closingCashOut`  | 当営業日の出金依頼を締める                        | -      |
| `/api/system/job/daily/realizeCashflow` | 入出金キャッシュフローを実現する(受渡日に残高へ反映) | -      |

*※POST*

### 配布用jarの作成

Spring BootではFat Jar(ライブラリなども内包するjar)を作成する事で単一の配布ファイルでアプリケーションを実行することができます。

1. コンソールから「gradlew build」を実行
1. `build/libs`直下にjarが出力されるのでJava8以降の実行環境へ配布
1. 実行環境でコンソールから「java -jar xxx.jar」を実行して起動

*※実行引数に「-Dspring.profiles.active=[プロファイル名]」を追加する事でapplication.ymlの設定値を変更できます。*  

### 本サンプルを元にしたプロジェクトリソースの作成

本サンプルを元にしたプロジェクトリソースを作成したい場合は以下の手順を実行してください。

1. build.gradle 内の拡張タスク(copyProject)の上部に定義されている変数を修正
1. コンソールから「gradlew copyProject」を実行

以降のプロジェクトインポート手順は前述のものと同様となります。

### 依存ライブラリ

| ライブラリ               | バージョン | 用途/追加理由 |
| ----------------------- | -------- | ------------- |
| `spring-boot-starter-*` | 1.3.0    | Spring Boot基盤 (actuator/security/aop/cache/web) |
| `spring-orm`            | 4.2.0    | Spring4のORM概念サポート |
| `hibernate-*`           | 5.0.0    | DB永続化サポート (core/java8/ehcache) |
| `ehcache-core`          | 2.6.+    | 最新のEhCache設定記法を利用するため |
| `HikariCP`              | 2.3.+    | コネクションプーリング実装の組み立て用途 |
| `jackson-datatype-*`    | 2.6.+    | JSON変換時のJava8/Hibernate対応 |
| `commons-*`             | -        | 汎用ユーティリティライブラリ |
| `icu4j-*`               | 54.1.+   | 文字変換ライブラリ |

*※実際の詳細な定義は`build.gradle`を参照してください*

### 補足解説（インフラ層）

インフラ層の簡単な解説です。

*※細かい概要は実際にコードを読むか、「`gradlew javadoc`」を実行して「`build/docs`」に出力されるドキュメントを参照してください*

#### DB/トランザクション

`sample.context.orm`直下。ドメイン実装をよりEntityに寄せるためのORMサポート実装です。Repository(スキーマ単位で定義)にドメイン処理を記載しないアプローチのため、Spring Bootが提供するJPA実装は利用していません。  
トランザクション定義はトラブルの種となるのでアプリケーション層でのみ許し、なるべく狭く限定した形で付与しています。単純なreadOnlyな処理のみ`@Transactional([Bean名称])`を利用してメソッド単位の対応を取ります。

スキーマは標準のビジネスロジック用途(`DefaultRepository`)とシステム用途(`SystemRepository`)の2種類を想定しています。Entity実装ではスキーマに依存させず、引数に渡す側(主にアプリケーション層)で判断させます。

#### 認証/認可

`sample.context.security`直下。顧客(ROLE_USER) / 社員(ROLE_ADMIN)の2パターンを想定しています。それぞれのユーザ情報(UserDetails)提供手段は`sample.usecase.SecurityService`において定義しています。

認証/認可の機能を有効にするには`application.yml`の`extension.security.auth.enabled`に`true`を設定してください(標準ではテスト用途にfalse)。顧客/社員それぞれ同一VMでの相乗りは考えていません。社員専用モードで起動する時は起動時のプロファイル切り替え等で`extension.security.auth.admin`を`true`に設定してください。

#### 利用者監査

`sample.context.audit`直下。「いつ」「誰が」「何をしたか」の情報を顧客/システムそれぞれの視点で取得します。アプリケーション層での利用を想定しています。ログインした`Actor`の種別(User/System)によって書き出し先と情報を切り替えています。運用時に行動証跡を取る際に利用可能です。

#### 例外

汎用概念としてフィールド単位にスタックした例外を持つ`ValidationException`を提供します。  
例外は末端のUI層でまとめて処理します。具体的にはアプリケーション層、ドメイン層では用途別の実行時例外をそのまま上位に投げるだけとし、例外捕捉は`sample.context.rest`直下のコンポーネントにおいてAOPを用いた暗黙的差し込みを行います。

#### 日付/日時

`sample.context.Timestamper`を経由してJava8で追加された`time`ライブラリを利用します。休日等を考慮した営業日算出はドメイン概念が含まれるので`sample.model.BusinessDayHandler`で別途定義しています。

#### キャッシング

`AccountService`等でSpringが提供する@Cacheableを利用しています。UI層かアプリケーション層のどちらかに統一した方が良いですが、本サンプルではアプリケーション層だけ付与しています。Hibernateの2nd/QueryキャッシュはEntity内で必要になる以外、利用しないことを推奨します。

#### テスト

パターンとしては通常のSpringコンテナを用いる2パターン(WebMockテスト/コンテナテスト)と、Hibernateだけに閉じた実行時間に優れたテスト(Entityのみが対象)の合計3パターンで考えます。（それぞれ基底クラスは `WebTestSupport` / `UnitTestSupport` / `EntityTestSupport`）  
テスト対象にServiceまで含めるてしまうと冗長なので、そこら辺のカバレッジはあまり頑張らずに必要なものだけとしています。

### License

本サンプルのライセンスはコード含めて全て*MIT License*です。  
Spring Bootを用いたプロジェクトの立ち上げ時に実装サンプルとして利用してください。
