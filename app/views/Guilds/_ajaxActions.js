
var guildToggleRecruitmentStatusAction = #{jsAction @Guilds.toggleRecruitmentStatus(':guildId') /}
var guildUpdateRecruitmentPolicyAction = #{jsAction @Guilds.updateRecruitmentPolicy(':guildId') /}
var guildUpdateWelcomeMessageAction = #{jsAction @Guilds.updateWelcomeMessage(':guildId') /}
var guildAddWallMessageAction = #{jsAction @Guilds.addWallMessage(':guildId') /}
var guildPollVote = #{jsAction @Guilds.pollVote(':guildId') /}
var guildSortMembersListAction = #{jsAction @Guilds.sortMembersList(':guildId') /}

var playerSuggestionsAction = #{jsAction @Application.searchPlayer(':query') /}