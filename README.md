## Android Webview App

This Android app connects to a website to display most of the content using a WebView and a JavaScriptInterface. 

The website has an API that uses both token and session based access. The app should store the token after asking for username and password. The endpoint accounts/login/ is the default django.contrib.auth.views.login view, but it might be preferable to use the view /api-token-auth/ which accepts username and password fields and returns an API token for that user. Once logged in the user should be able to view the url /
