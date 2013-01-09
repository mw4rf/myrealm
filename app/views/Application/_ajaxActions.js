var ajaxLoginAction = #{jsAction @Application.ajaxLogin(':playerName') /}
var ajaxPasswordAction = #{jsAction @Application.ajaxPassword(':playerName') /}

var playerSuggestionsAction = #{jsAction @Application.searchPlayer(':query') /}