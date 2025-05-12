# Sora Android SDK Messaging Sample

時雨堂の提供する [Sora Android SDK](https://sora-android-sdk.shiguredo.jp/) を使用して、リアルタイムにメッセージを送受信するサンプルアプリケーションです。

## 動かし方

gradle.properties を作成し、`signaling_endpoint` と `channel_id` を設定します。

Sora Labo などを利用する場合は、`signaling_metadata` にアクセストークンを設定してください。

```bash
cp gradle.properties.example gradle.properties
```

あとはアプリをビルドして実機で動かしてください。

TODO(zztkm): スクリーンショットを用意する

## 実装機能リスト

- [x] メッセージ受信
- [ ] メッセージ送信

