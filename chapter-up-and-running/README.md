Akka実践バイブルのサンプルコードのJava版
==============

Akka実践バイブル「第2章　最小のAkkaアプリケーション」のサンプルコードをJavaに置き換えてみた。

### 必要な環境
* JDK8以上
* Maven（任意：インストールしない場合は[maven-wrapper](https://github.com/takari/maven-wrapper)により `mvn` を`./mvnw` で代替可能）

### ソースコードの取得
```
git clone <xxx.git>
```

### コンパイル・サーバー起動
- `chapter-up-and-running` ディレクトリで実行
```
mvn compile exec:exec
```

### テスト
- `chapter-up-and-running` ディレクトリで実行
```
mvn test
```

### APIエンドポイント

| 機能 | HTTPメソッド | パス | JSON |
| --- | ----- | ---- | --- |
| イベント作成 | POST | /events/<イベント名>/ | {"tickets":<枚数>} |
| チケット購入 | POST | /events/<イベント名>/tickets/ | {"tickets":<枚数>} |
| イベント一覧 | GET | /events/ | |
| イベント取得 | GET | /events/<イベント名>/ |
| イベントキャンセル | DELETE | /events/<イベント名>/ |


## ハンズオン

### クローンとビルドとインターフェイスのテスト

#### クローン
```
git clone https://github.com/yugolf/akka-in-action-java.git
```

#### ビルドとサーバーの起動
```
cd akka-in-action-java/chapter-up-and-running/
mvn compile exec:exec
```

#### インターフェイスのテスト([HTTPie](https://httpie.org/)による実行例)
##### イベント作成
```
http POST localhost:5000/events/JJUG/ tickets:=3
```
##### チケット購入
```
http POST localhost:5000/events/JJUG/tickets tickets:=2
```
##### イベント一覧
```
http GET localhost:5000/events
```
##### イベント取得
```
http GET localhost:5000/events/JJUG/
```
##### イベントキャンセル
```
http DELETE localhost:5000/events/JJUG/
```

### アプリケーションでのActorの探求
#### ハンズオン用ブランチ取得
```
git checkout handson
```

#### Exercise 0
```
mvn compile exec:exec
```

#### Exercise 1
```
mvn test -Dtest=RestApiTest#testCreateEvent
```

#### Exercise 2
```
mvn test -Dtest=RestApiTest#testBuy
```

#### Exercise 3
```
mvn test -Dtest=RestApiTest#testGetEvents
```

#### Exercise 4
```
mvn test -Dtest=RestApiTest#testCancel
```

### クラウドへ
#### Herokuのインストール
```
brew install heroku
```

#### Heroku上にアプリケーション作成
- `akka-in-action-java` ディレクトリで実行

```
heroku login
heroku create <アプリケーション名>
```

#### ソースコードをHeroku上にプッシュ
```
git subtree push --prefix chapter-up-and-running heroku master
```

#### Heroku上のアプリケーションへアクセス
```
http POST <アプリケーション名>.herokuapp.com/events/JJUG tickets:=250
http POST <アプリケーション名>.herokuapp.com/events/JJUG/tickets tickets:=4
```