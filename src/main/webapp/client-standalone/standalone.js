// selected  mep only
// if no list of comma separated ids is given, all meetings are displayed
var meetingsUrl = "https://askyouradmin/api/meetings/dt/"; // 41,9,8,44,45,46,47,48,54,55,56,26";

var lc2pShowStart = '1';
// Display column for start date?
var lc2pShowEnd = '1';
// Display column for first name? '1' : yes, else don't
var lc2pShowFirstName = '1';
// Display column for last name? '1' : yes, else don't
var lc2pShowLastName = '1';
// Display column for title? '1' : yes, else don't
var lc2pShowTitle = '1';
// Display column for partner? '1' : yes, else don't
var lc2pShowPartner = '1';
// Display column for tags? '1' : yes, else don't
var lc2pShowTags = '1';
// Display tags with title? '1' : yes, else don't
var lc2pShowTagsTitle = '';
// Default order at first load
var lc2pOrder = "startDate desc";

// see http://momentjs.com/docs 
var lc2pDateFormat = 'LLL';
var lc2pPerPage = 10;
var lc2pMomentLocale = "en";

if (lc2pDateFormat != "") {
	momentDateFormat = lc2pDateFormat;
}
if (lc2pMomentLocale != "") {
	momentLocale = lc2pMomentLocale;
}

(function(factory) {
	if (typeof define === "function" && define.amd) {
		define([ "jquery", "moment", "datatables" ], factory);
	} else {
		factory(jQuery, moment);
	}
}(function($, moment) {

	$.fn.dataTable.moment = function(format, locale) {
		var types = $.fn.dataTable.ext.type;

		console.log(momentLocale);
		moment.locale(momentLocale);
		// Add type detection
		types.detect.unshift(function(d) {
			// Strip HTML tags if possible
			if (d && d.replace) {
				d = d.replace(/<.*?>/g, '');
			}

			// Null and empty values are acceptable
			if (d === '' || d === null) {
				return 'moment-' + format;
			}

			return moment(d, format, locale, true).isValid() ? 'moment-'
					+ format : null;
		});

		// Add sorting method - use an integer for the sorting
		types.order['moment-' + format + '-pre'] = function(d) {
			return d === '' || d === null ? -Infinity : parseInt(moment(
					d.replace ? d.replace(/<.*?>/g, '') : d, format, locale,
					true).format('x'), 10);
		};
	};

}));

$(document).ready(function() {
	$.fn.dataTable.moment(momentDateFormat);

	var dt = $('#lobbycal').DataTable({
		'ajax' : meetingsUrl,
		'serverSide' : true,
		"info" : false,
		"processing" : true,
		"defaultContent" : "-",
		"bLengthChange" : false,
		"oLanguage" : {
			"sProcessing" : "Searching...",
			"sZeroRecords" : "No meetings found",
			"emptyTable" : "No meetings found"
		},
		columns : [ {
			data : 'startDate',
			visible : (lc2pShowStart == "1"),
			name : 'startDate',
			render : function(date, type, full) {
				return moment(date).format(momentDateFormat)
			}
		}, {
			data : 'endDate',
			visible : (lc2pShowEnd == "1"),
			name : 'endDate',
			render : function(date, type, full) {
				return moment(date).format(momentDateFormat)
			}
		}, {
			data : 'userFirstName',
			searchable : false,
			visible : (lc2pShowFirstName == "1"),
			name : 'userFirstName'
		}, {
			data : 'userLastName',
			searchable : false,
			visible : (lc2pShowLastName == "1"),
			name : 'userLastName'
		}, {
			data : 'mPartner',
			visible : (lc2pShowPartner == "1"),
			name : 'partner',
			orderable : false,
			render : function(data, type, row) {
				if (row.mPartner) {
					return data;
				} else {

					return partners(row.partners);
				}
			}
		}, {
			data : 'title',
			visible : (lc2pShowTitle == "1"),
			name : 'title',
			render : function(data, type, row) {
				if (lc2pShowTagsTitle == "1") {
					return data + " " + mTags(row.mTag.split(" "));
				} else {
					return data;
				}
			}
		}, {
			data : 'mTag',
			visible : (lc2pShowTags == "1"),
			name : 'tag',
			orderable : false,
			render : function(data, type, row) {
				return mTags(data.split(" "));
			}
		} ],
		pageLength : parseInt(lc2pPerPage),
		lengthMenu : [ parseInt(lc2pPerPage) ]
	});
	var order = lc2pOrder.split(' ', 2)[1];
	var cname = lc2pOrder.split(' ', 1)[0]

	var cindx = dt.column(cname).index();
	if (cindx === undefined) {
		cindx = 1;
	}
	dt.order([ cindx, order ]).draw();
	$('#lobbycal tbody').on('click', 'span.tag', function() {
		// var cell = dt.cell( $(".tag"));
		$('.dataTables_filter input').val(this.innerHTML);
		$('.dataTables_filter input').click();
		dt.search(this.innerHTML).draw();
	});

});

function partners(partners) {
	var res = "";
	$
			.each(
					partners,
					function(key, partner) {
						if (partner.transparencyRegisterID != "") {
							res += ('<a href="http://ec.europa.eu/transparencyregister/public/consultation/displaylobbyist.do?id='
									+ partner.transparencyRegisterID
									+ '">'
									+ partner.name + '</a>');
						} else {
							if (partner.name != "") {
								res += (partner.name);
							}
						}
						res = "<span class=\"partner\">" + res + "</span><br/>";
					});
	return res;
}

function mTags(tags) {
	var res = "";

	jQuery.each(tags, function(key, tag) {
		if (tag != "") {
			res += "<span title=\""+tag+"\" class=\"labeled tag " + (tag) + "\">" + (tag)
					+ "</span><br/>";
		}
	});
	return res;
}
