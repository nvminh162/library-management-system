Tìm ra nguyên nhân! `AXONIQ_AXONSERVER_SERVERS=axonserver:**8024**` sai port.

| Port | Mục đích |
|------|----------|
| `8024` | HTTP / Dashboard UI |
| `8124` | **gRPC** — port để client (xxxx-service) kết nối |
| `8224` | Management |

book-service kết nối qua gRPC nên phải dùng port `8124`: 

```
https://stackoverflow.com/questions/67984249/connecting-to-axonserver-node-failed-unavailable-network-closed-for-unk
```