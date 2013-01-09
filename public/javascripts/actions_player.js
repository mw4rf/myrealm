



/**
 *  AJAX LOGIN  *
*/

function ajaxlogin() {
	var playerName = $('#playerName').val()
	$.post(ajaxLoginAction({playerName: playerName}), function(data) {
		$("#ajaxLoginForm").html(data)
	})
}

function ajaxpassword(playerName) {
	var password = $('#password').val()
	$.post(ajaxPasswordAction({playerName: playerName}), {password: password}, function(data) {
		$("#ajaxLoginForm").html(data)
	})
}
