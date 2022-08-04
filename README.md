<h1 align="center">Bit <p font size="14sp">~Insta Viewer with expand button~</p></h1>
<p align="center">
  <img src="https://user-images.githubusercontent.com/102008212/180078651-1d7b3f98-d624-4843-ae8c-7afe3c03433a.jpg" width="240dp" /><br>
<br><br><br>

##   <img src="https://user-images.githubusercontent.com/102008212/180079714-0d0af206-38c5-4f0a-a91b-32e1396f9f2a.png" width="26px;" /> Main function

<h6>Instagram has a function called "album posting", which allows you to combine multiple photos into one post. However, the viewer has to scroll over and over, which is troublesome. Therefore, I created an application that displays a list of posts with an expand button by searching for a user name.
　
 
## 🌐 How to use

<h6>This app uses Instagram Graph API. Therefore, you need to get facebook, instagram account, business account id and access token for the app (<a href="https://blog.dtn.jp/2022/02/02/instagram-graph-api-ver12/">this link</a> can help you to get them). In addition, you need to add Secret.kt to the project folder if you build this program. The contents of Secret.kt are as follows.</h6>

```kotlin
private const val BUSINESS_ACCOUNT_ID = "[YOUR BUSINESS ACCOUNT ID]"
private const val MEDIA_FIELDS = "media_type,media_url,children{media_type,media_url}"
private const val ACCESS_TOKEN = "[APP ACCESS TOKEN]"
class Secret {
    companion object {
        fun requestUrlFormatter() = "https://graph.facebook.com/v14.0/${BUSINESS_ACCOUNT_ID}?fields=" +
                "business_discovery.username(%s){profile_picture_url,name,media%s{${MEDIA_FIELDS}}}&access_token=${ACCESS_TOKEN}"
    }
}
```


## 📦 TODO

<h6>・Fix profile icon acquisition error</h6>
<h6>・Add function to display next image by sliding</h6>
<p>(I want to improve readability...)
　

## 👀 Author

- [Github](https://github.com/norram0123)
- Twitter - I don't have...
