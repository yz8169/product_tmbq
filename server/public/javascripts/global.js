var zhRunning = "正在运行"
var zhInfo = "信息"

$.ajaxSetup(
    {
        cache: false,
    }
)


function inputSelect(element) {
    $(element).select()
}

function extractor(query) {
    var result = /([^\n]+)$/.exec(query);
    if (result && result[1])
        return result[1].trim();
    return '';
}

var matcherRegex = /[^\n]*$/
var matcherEnd = "\n"
var num = 3

function proteinIDFmt(value, row, index) {
    return "<a onclick=\"showExp('" + value + "')\">" + value + "</a>"
}

function metaboliteIDFmt(value, row, index) {
    return "<a onclick=\"showMExp('" + escape(value) + "')\">" + value + "</a>"
}

$(function () {

    $("[data-toggle='popover']").popover()

})
