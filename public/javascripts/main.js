/**
 * Loaded with the document (jQuery binding)
 */
$(document).ready(function() {

	// Confirmation dialogs
	$("#dialog").dialog({
		autoOpen : false,
		modal : true
	});

}); // end document.ready

// JQuery UI bindings
$(".draggable").draggable()
$(".resizable").resizable()

/** Color pickers **/
$(".colorpicker").jPicker(
{
	window: {
		expandable:true,
		position: {
			x: 'center',
			y: 'bottom'
		}
	}
}, 
function(color, context) {
	var hex = color.val('all').hex;
	if($(this).hasClass("fg")) {
		$("input[type='hidden'][name='fgcolor']").val(hex)
	}
	else if($(this).hasClass("bg")) {
		$("input[type='hidden'][name='bgcolor']").val(hex)
	}
})

/**
 * Modal dialogs
 * (to confirm delete item & other sensitive things)
 */
$(".confirmationLink").live('click', function(e) {
	e.preventDefault();
	var targetUrl = $(this).attr("href");

	var message = $(this).attr("message");
	$("#dialog").html(message)

	$("#dialog").dialog({
		buttons : {
			"Ok" : function() {
				window.location.href = targetUrl;
			},
			"Cancel" : function() {
				$(this).dialog("close");
			}
		}
	});

	$("#dialog").dialog("open");
});

/**
 * Show a confirmation popup before proceeding, giving the user the opportunity to cancel.
 * usage: <a href="..." class="ajaxConfirmationLink" onConfirmation="myJSfunction" message="r u sure ?">
 */
$(".ajaxConfirmationLink").live('click', function(e) {
	e.preventDefault();
	var targetFunction = $(this).attr("onConfirmation");

	var message = $(this).attr("message");
	$("#dialog").html(message)

	$("#dialog").dialog({
		buttons : {
			"Ok" : function() {
				eval(targetFunction)
				$(this).dialog("close")
				return false
			},
			"Cancel" : function() {
				$(this).dialog("close")
			}
		}
	});

	$("#dialog").dialog("open");
});

$(".infoLink").live('click', function(e) {
	e.preventDefault()
	var message = $(this).attr("message")
	var diagType = $(this).attr("dialog-type")
	var title = $(this).attr("dialog-title")
	var height = $(this).attr("dialog-height")
	var width = $(this).attr("dialog-width")

	if(diagType == "html")
		$("#dialog").html(message)
	else
		$("#dialog").text(message)

	if(title != null)
		$("#dialog").dialog({
			title: title
		})

	if(height != null && width != null)
		$("#dialog").dialog({
			height: height,
			width: width
		})

	$("#dialog").dialog({
		buttons : {
			"Ok" : function() {
				$(this).dialog("close");
			}
		}
	});

	$("#dialog").dialog("open");
});

/**
 * Toggle show/hide a div
 */
function toggleshow(container) {
    toggleshow(container, true)
}

function toggleshow(container, fixsize) {
    // Set a fixed height to fix the "jumpyness" bug
	if(fixsize) {
	    $(container).css('height', $(container).height() + 'px')
	}
	$(container).slideToggle()
	return false;
}

/**
 *
 * Syntax :
 * 		<a href="#" class="toggle" toggle="#target"> // toggle the element identified by its id #target
 * 		<a href="#" class="toggle" toggle=".target"> // toggle all elements with the given CSS class
 * 		<a href="#" class="toggle sibling" toggle=".target"> // toggle all elements with the given CSS class within the same container
 * 		<a href="#" class="toggle sibling" toggle="#target" parent=".parent"> // toggle all elements with the given CSS class within the parent found by its CSS class .parent
 *
 */
$('.toggle').live('click', function() {
	var target = $(this).attr('toggle')
	if($(this).hasClass('sibling')) {
		if($(this).attr('parent') != null)
			var parent = $(this).parents($(this).attr('parent'))
		else
			var parent = $(this).parent()
		parent.find(target).slideToggle()
	}
	else {
		$(target).slideToggle()
	}
	// Set next selected element
	if($(this).hasClass('selectNext')) {
		var nxt = $('#' + $(this).attr('selectNext'))
		nxt.focus()
	}
	// return
	return false;
})

function loading(show) {
	if(show)
		$('#loading').removeClass('loading-hidden').addClass('loading-shown')
	else
		$('#loading').removeClass('loading-shown').addClass('loading-hidden')
}













