# (WIP) Run graal native binary on Aws Lambda

## 概要

Graal で Native 化したバイナリを、 AWS Lambda の custom runtimeで動かそうと試みましたが動きませんでした。

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


