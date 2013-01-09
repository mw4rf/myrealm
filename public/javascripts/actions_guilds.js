/**
 * Contenteditable items
 */

$('#guildRecruitmentPolicy').live('blur', function(){
    var content = $(this).html();
    var guildId = $(this).attr("guildId")
    $.post(guildUpdateRecruitmentPolicyAction({guildId:guildId}), {content:content}, function(data) {
    	// no callback needed
    })
})

$('#guildRecruitmentStatusEditable').live('click', function(){
    var guildId = $(this).attr("guildId")
    $.post(guildToggleRecruitmentStatusAction({guildId:guildId}), function(data) {
    	$('#guildRecruitmentStatusResponse').html(data)
    })
})

$('#guildWelcomeMessage').live('blur', function(){
    var content = $(this).html();
    var guildId = $(this).attr("guildId")
    $.post(guildUpdateWelcomeMessageAction({guildId:guildId}), {content:content}, function(data) {
    	// no callback needed
    })
})

$('#guildWallMessageSubmit').live('click', function(){
    var guildId = $(this).attr("guildId")
    var content = $("#guildWallMessageContent").val()
    $('#guildWallMessageSubmit').attr("disabled", true) // disable button to prevent click-fury
    $.post(guildAddWallMessageAction({guildId:guildId}), {content:content}, function(data) {
    	$('#guildWall').prepend(data)
    	$('#guildWallMessageSubmit').attr("disabled", false)
    	$('#guildWallMessageContent').val("")
    })
})

function sortMembersList(guildId) {
	var sorting = $('#guildMembersSort').val()
	var order = $('#guildMembersOrder').val()
	$.post(guildSortMembersListAction({guildId:guildId}), {sorting:sorting, order:order}, function(data) {
		$('#guildMembersList').html(data)
	})
}
$('#guildMembersSort').live('change', function() {
	sortMembersList($(this).attr("guildId"))
})
$('#guildMembersOrder').live('change', function() {
	sortMembersList($(this).attr("guildId"))
})

/** POLLS **/

$('#pollSubmit').live('click', function() {
	var optionId = $('#pollOptions input[type=radio]:checked').val();
	var pollId = $('#pollOptions').attr('pollId');
	var guildId = $('#pollOptions').attr('guildId');
	$.post(guildPollVote({guildId:guildId}), {pollId:pollId, optionId:optionId}, function(data) {
		$('#guildPoll').html(data)
	})
})

/** Guild Admin **/

$('#guildLogosSelect').live('change', function() {
	var id = $(this).val()
	$('#guildLogoShow').html("<img src='/public/images/guilds/banner" + id + ".png' />")
})