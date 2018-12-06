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

+ 処理速度向上のため、イベントループ実装を Java ですべて行うようにした。
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
bootstarp をバイナリにする場合、 シェルの bootstrap は不要。

```
mv my-graal bootstrap
chmod + bootstrap
zip my-graal.zip bootstrap
```

## パフォーマンス


### Cold Start

Memory: 79MB
Duration: 380ms

```
START RequestId: 1cce68d8-f902-11e8-b471-0b8fa675139e Version: $LATEST
1cce68d8-f902-11e8-b471-0b8fa675139e Answer is 24
ACCEPTED {"status":"OK"}

END RequestId: 1cce68d8-f902-11e8-b471-0b8fa675139e
REPORT RequestId: 1cce68d8-f902-11e8-b471-0b8fa675139e	Init Duration: 250.27 ms	Duration: 132.84 ms	Billed Duration: 400 ms 	Memory Size: 128 MB	Max Memory Used: 79 MB		
```

### ホットスタート


### Cold Start

Memory: 74MB
Duration: 30.1ms

```
START RequestId: 3d205575-f902-11e8-9fcd-4ff8d4d3054e Version: $LATEST
3d205575-f902-11e8-9fcd-4ff8d4d3054e Answer is 24
ACCEPTED {"status":"OK"}

END RequestId: 3d205575-f902-11e8-9fcd-4ff8d4d3054e
REPORT RequestId: 3d205575-f902-11e8-9fcd-4ff8d4d3054e	Duration: 31.56 ms	Billed Duration: 100 ms 	Memory Size: 128 MB	Max Memory Used: 74 MB	

```

