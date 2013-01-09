   var adventuresSortAction = #{jsAction @Adventures.sortedIndex(':playerName', ':sorting', ':showonly', ':selforparticipations', ":limit") /}
   var adventuresAddParticipantAction = #{jsAction @Adventures.addParticipant(':playerName', ':adventureId', ":participant") /}
   var adventuresRemoveParticipantAction = #{jsAction @Adventures.removeParticipant(':playerName', ':adventureId', ":participant") /}
   var adventuresEditCommentAction = #{jsAction @Adventures.editComment(":commentId") /}
   var adventuresAddCommentAction = #{jsAction @Adventures.addComment(":adventureId") /}
   
   var playerSuggestionsAction = #{jsAction @Application.searchPlayer(':query') /}