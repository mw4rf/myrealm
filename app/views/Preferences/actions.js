
var updatePreferencesAction = #{jsAction @Preferences.update(':playerName') /}

/**
 * Form submission & response for CHECKBOXES
 */
$('input[type=checkbox]').live('change', function() {
	var item = $(this)
	var playerName = '${player.name}'
	var key = item.attr('name')
	var value = item.is(':checked')
	// Remove previous responses, if any
	item.siblings(".label").remove()
	// Ajax request
	$.post(updatePreferencesAction({playerName:playerName}), {key:key, value:value}, function(data) {
		// Add response
		item.before(data)
	})
})

$('input[type=radio]').live('change', function() {
	var item = $(this)
	var playerName = '${player.name}'
	var key = item.attr('name')
	var value = item.val()
	// Remove previous responses, if any
	item.siblings(".label").remove()
	// Ajax request
	$.post(updatePreferencesAction({playerName:playerName}), {key:key, value:value}, function(data) {
		// Add response
		item.before(data)
	})
})

/**
 * Form submission & response for SUBMIT INPUTS
 */
$('input[type=submit]').live('click', function() {
	var item = $(this)
	var playerName = '${player.name}'
	var field = $('[name=' + item.attr('target') + ']')
	var key = field.attr('name')
	var value = field.val()
	// Remove previous responses, if any
	item.siblings(".label").remove()
	// Ajax request
	$.post(updatePreferencesAction({playerName:playerName}), {key:key, value:value}, function(data) {
		// Add response
		item.after(data)
	})
})

/**
 * Form submission & response for SELECT elements with the .submittable class
 */
$('select.submittable').live('change', function() {
	var item = $(this)
	var playerName = '${player.name}'
	var key = item.attr('name')
	var value = item.val()
	// Remove previous responses, if any
	item.siblings(".label").remove()
	// Ajax request
	$.post(updatePreferencesAction({playerName:playerName}), {key:key, value:value}, function(data) {
		// Add response
		item.after(data)
	})
})