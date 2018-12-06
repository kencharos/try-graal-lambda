#  Run graal native binary on Aws Lambda

## 概要

Graal で Native 化したバイナリを、 AWS Lambda の custom runtimeで動かしました。

## 手順

```
./gradlew clean assemble
docker build -t function .
```

実行コンテナから、ビルドされた my-graal を取得し、 bootstrap と一緒に zip化して Lamdba にデプロイする。

入力は次のJSON

```
{
    "v1":1
    "v2":3
}
```

## micronaut graal sample からの修正点

+ bootstrap 中でイベントループを実行するようにし、CLIアプリはループ一回分の処理だけを行う。
    + bootstrap がCLIアプリの結果を受け取る方法としてファイルを採用した。
    + <バイナリ> -r <リクエストID> -d <イベントデータ> -o <成功結果ファイル> -e <失敗結果ファイル>
    + 上記の実行で、成功結果ファイルの中身があれば成功としてその中身を response エンドポイントに送信する
    + 成功結果ファイルの中身が無い場合は、失敗結果ファイルの中身えお error エンドポイントに送信 
+ custom runtimeで動かすため、 piccoli 形式の CLIアプリケーションとした
+ piccoli を native化するため、 CommandClass や 自作DTOのリフレクション定義ファイルを作成し、ビルド時に追加するようにした。
+ micronaut 1.0.0 は graal rc7に依存しているが、 rc7は maven から消えたたため、 rc9 にした。
+ rc9 でビルドするため  jzlib を追加した
+ Dockerfile の native-image の引数に `-H:-UseServiceLoaderFeature` を追加した
+ -H:-AllowVMInspection を設定しないと Lambda で起動できなかったため修正
+ https://github.com/oracle/graal/issues/841 に基づき、`R:-InstallSegfaultHandler` を追加

## AnazonGraal の Dockerfileについて

Amazon Linux 上に Graal をインストールするもの。
意味はなかった。
このイメージを使う場合、AmaoznGraal ディレクトリで、
`docker build -t amazongraal:9` とし、その後 Dockerfile.alt でビルドを行う。

## コンテナからファイルの取得

コンテナを起動した後、
`docker cp <container id>:my-graal <host directory> ` でコンテナのファイルをホストに転送できる。

その後、次のようにして zip化する。

```
chmod + bootstrap
zip my-graal.zip my-graal bootstrap
```

## パフォーマンス

イベントループを curl で実装している関係か、少々遅め。

### Cold Start

Memory: 81MB
Duration: 2862ms

```
START RequestId: 6cec98ac-f8f9-11e8-a714-33c9118529bc Version: $LATEST
6cec98ac-f8f9-11e8-a714-33c9118529bc Answer is 24
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed

  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0
100    29  100    16  100    13    395    321 --:--:-- --:--:-- --:--:--   400
{"status":"OK"}
END RequestId: 6cec98ac-f8f9-11e8-a714-33c9118529bc
REPORT RequestId: 6cec98ac-f8f9-11e8-a714-33c9118529bc	Init Duration: 53.38 ms	Duration: 2862.32 ms	Billed Duration: 3000 ms 	Memory Size: 128 MB	Max Memory Used: 81 MB	
```

### ホットスタート


### Cold Start

Memory: 81MB
Duration: 478ms

```
START RequestId: 81f8a020-f8f9-11e8-8aa3-e12b01cc879f Version: $LATEST
81f8a020-f8f9-11e8-8aa3-e12b01cc879f Answer is 24
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed

  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0
100    29  100    16  100    13    209    170 --:--:-- --:--:-- --:--:--   800
{"status":"OK"}
END RequestId: 81f8a020-f8f9-11e8-8aa3-e12b01cc879f
REPORT RequestId: 81f8a020-f8f9-11e8-8aa3-e12b01cc879f	Duration: 478.25 ms	Billed Duration: 500 ms 	Memory Size: 128 MB	Max Memory Used: 81 MB	

```

