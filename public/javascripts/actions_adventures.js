function selectAdventureChange(select) {
	var imgid = "/public/" + $(select.options[select.selectedIndex]).attr("imgid")
	$('#adventureImage').html("<img align=\"middle\" src=\"" + imgid + "\" />")
}

function adventures_sortedIndex(playerName) {
	var sortc = $("#adventures_sorting_combo").val()
	var restr = $("#adventures_showonly_combo").val()
	var sorp =  $("#adventures_selforpart_combo").val()
	var tlast =  $("#adventures_thelast_combo").val()
	$.get(adventuresSortAction({playerName: playerName, sorting: sortc, showonly: restr, selforparticipations: sorp, limit: tlast}), function(data) {
		$("#adventures_list").fadeOut(400, function() {
			$("#adventures_list").html(data)
			$("#adventures_list").fadeIn()
		})
	})
}

function adventures_addParticipant(playerName, adventureId) {
	var participant = $("#adventure_participant_field").val()
	$.post(adventuresAddParticipantAction({playerName: playerName, adventureId: adventureId, participant: participant}), function(data) {
		$("#participants_list").html(data)
	})
}

function adventures_removeParticipant(playerName, adventureId) {
	var participant = $("#adventure_removeParticipantSelect").val()
	$.post(adventuresRemoveParticipantAction({playerName:playerName, adventureId:adventureId, participant: participant}), function(data) {
		$("#participants_list").html(data)
		$("#adventure_removeParticipantSelect option[value='" + participant + "']").remove()
	})
}

function adventures_editcomment(commentId) {
	var comment = $("#comment_area_" + commentId).val()
	$.post(adventuresEditCommentAction({commentId: commentId}), {text:comment}, function(data) {
		$("#comment_" + commentId).fadeOut('slow', function(){
			$(this).replaceWith(data)
			$("#comment_" + commentId).fadeIn('slow')
		})
	})
}

function adventures_addcomment(adventureId) {
	var comment = $("#comment_area").val()
	var author = $("#comment_author").val()
	$.post(adventuresAddCommentAction({adventureId:adventureId}), {author:author,text:comment}, function(data) {
		$(data).fadeIn().appendTo($("#comments_list"))
		$('#comment_area').val('')
	})
}
