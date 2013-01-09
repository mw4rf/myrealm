/**
 * Add a new friend
 */
var addFriendAction = #{jsAction @Home.addFriend(':playerName', ':friendName') /}
$("button[action=addFriend]").live('click', function() {
	var friendName = $("#addFriend").val()
	var playerName = $("#playerName").val()
	$.get(addFriendAction({playerName:playerName, friendName:friendName}), function(data) {
		$("#friendsList").html(data)
	})
})

/**
 * Remove existing friend
 */
var removeFriendAction = #{jsAction @Home.removeFriend(':playerName', ':friendName') /}
$("button[action=removeFriend]").live('click', function() {
	var friendName = $("#removeFriend").val()
	var playerName = $("#playerName").val()
	$.get(removeFriendAction({playerName:playerName, friendName:friendName}), function(data) {
		$("#friendsList").html(data)
		$("#removeFriend option[value='" + friendName + "']").remove()
	})
})
