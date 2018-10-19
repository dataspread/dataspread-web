// import navigation container
var navContainer = document.getElementById('navChart');
var currLevel = 0;
var levelList = [];
var spanList = [];
var cumulativeData = [];
var viewData;
var mergeCellInfo = [];
var colHeader = [];
var cumulativeDataSize = 0;
var nav;
var clickable = true;
var nextPath = "";
var prevPath = "";
var navHistroyTable = {};
var navHistoryPathIndex = [];
var breadCrumbHistoryPathIndex = [];

var options = [];
var hieraOpen = false;
var exploreOpen = false;
var funcOptions =
    [
        "AVEDEV", "AVERAGE", "COUNT", "COUNTA", "COUNTBLANK", "COUNTIF",
        "DEVSQ", "LARGE", "MAX", "MAXA", "MIN", "MINA",
        "MEDIAN", "MODE", "RANK", "SMALL", "STDEV", "SUBTOTAL",
        "SUM", "SUMIF", "SUMSQ", "VAR", "VARP"
    ];
var subtotalFunc =
    [
        "AVERAGE", "COUNT", "COUNTA", "MAX", "MIN", "PRODUCT", "STDEV", "SUM",
        "VAR", "VARP"
    ];
var pointFunc =
    [
        "MIN", "MAX", "MEDIAN", "MODE", "RANK", "SMALL", "LARGE", "COUNTIF", "SUMIF"
    ];
var currData;
var zoomming = false;
var zoomouting = false;
var targetChild;
var currentFirstRow=0;
var currentLastRow=40;

var sortOptionString = "";
var sortTotalNum = 0;

var aggregateTotalNum = 0;
var aggregateColStr = "";
var aggregateOptStr = "";
var aggregateData = {};
var navRawFormula;
var navAggRawData = [];

var exploreAttr;
var hierarchicalColAttr = [];
var sortAttrIndices = [];

var selectedChild = [];
var selectedBars = [];
var lowerRange;
var upperRange;

var currRange;

var isBucketNumeric = false;

// showing exploration options and create corresponding html
$("#Explore").click(function () {
    lowerRange = 0;
    upperRange = 1000;

    $.get(baseUrl + 'getSortAttrs/' + bId + '/' + sName, function (data) {
        var $dropdown = $("#exploreOpt");
        $dropdown.empty();
        $dropdown.append(
            "<legend class=\"form-label\" style=\"font-size:1rem\">Attribute Name</legend>\n");
        options = data.data
        for (let i = 0; i < options.length; i++) {
            let tempString = "<div class='form-check'>" +
                "<input class='form-check-input' " +
                "type='radio' name='exploreValue' id='Radios" + i +
                "' value='" + (i + 1) + "'>" +
                "<label class='form-check-label' for='Radios" + i +
                "'> " + options[i] + "</label></div>"
            $dropdown.append(tempString);
        }

        // clear cumulative string for aggregate attribute, function and
        // sortattribute
        aggregateColStr = "";
        aggregateOptStr = "";
        sortOptionString = "";

        var $aggregateCol = $("#aggregateCol");
        $aggregateCol.empty();
        aggregateTotalNum = 0
        $aggregateCol.append(createAggreString());

        // customize input field for different formula
        $("#aggregateOpt0").change(function () {
            // Do something with the previous value after the change
            let tempString;
            console.log($(this))
            $("#add0").nextAll().remove();
            switch (this.value) {
                case "COUNTIF":
                case "SUMIF":
                    $(this).parent().append(
                        "<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
                    break;
                case "LARGE":
                case "SMALL":
                    $(this).parent().append(
                        "<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
                    break;
                case "SUBTOTAL":
                    tempString =
                        "<select class='' id='aggrePara0'><option value='' disabled selected hidden>Function_num</option>";
                    for (let i = 0; i < subtotalFunc.length; i++) {
                        tempString += "<option value='" + (i + 1) + "''>" + subtotalFunc[i] +
                            "</option>";
                    }
                    tempString += "</select>";
                    $(this).parent().append(tempString);
                    break;
                case "RANK":
                    tempString =
                        "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>";
                    tempString +=
                        "<select class='' id='aggrePara00'><option value='1' selected >ascending</option><option value='0'>descending</option></select>"
                    $(this).parent().append(tempString);
                    break;
            }
        });

        var $sortDropdown = $("#inlineOpt");
        $sortDropdown.empty();
        sortTotalNum = 0;
        $sortDropdown.append(createSortString());
    });

    hieraOpen = false;
    if (exploreOpen) {
        hot.updateSettings({
            width: wrapperWidth - $("#navChart").width() - wrapperWidth * 0.19,
        });
    } else {
        hot.updateSettings({
            width: wrapperWidth * 0.79,
        });
    }
    $("#hierarchical-col").css("display", "none");
    $("#bucket-col").css("display", "none");
    $("#test-hot").css({"float": "left"});
    $("#exploration-bar").css({
        "display": "inline",
        "float": "left",
        "width": "19%",
        "height": wrapperHeight * 0.95
    });
})


$(document).on("click", ".hierRemove", function (e) {
        if (aggregateTotalNum > 1) {
            let id = e.target.id.slice(-1);
            $("#aggregateCol").children()[id].remove();

            for (let i = Number(id) + 1; i < aggregateTotalNum; i++) {
                console.log($("#aggregateCol"));
                $("#line" + i).prop('id', "line" + (i - 1))
                $("#rm" + i).prop('id', "rm" + (i - 1));
                $("#add" + i).prop('id', "add" + (i - 1));
                $("#aggregateCol" + i).prop('id', "aggregateCol" + (i - 1));
                $("#aggregateOpt" + i).prop('id', "aggregateOpt" + (i - 1));
            }
            aggregateTotalNum -= 1;
        } else {
            alert("You cannot remove all options.")
        }

    }
);
$(document).on("click", ".hierAdd", function (e) {
        if (aggregateTotalNum < 9) {
            let id = e.target.id.slice(-1);

            for (let i = aggregateTotalNum - 1; i > Number(id); i--) {
                $("#line" + i).prop('id', "line" + (i + 1))
                $("#rm" + i).prop('id', "rm" + (i + 1));
                $("#add" + i).prop('id', "add" + (i + 1));
                $("#aggregateCol" + i).prop('id', "aggregateCol" + (i + 1));
                $("#aggregateOpt" + i).prop('id', "aggregateOpt" + (i + 1));
            }

            $(createAggreString(Number(id) + 1)).insertAfter("#line" + id)
            $("#aggregateOpt" + (Number(id) + 1)).change(function (e) {
                let number = e.target.id.charAt(e.target.id.length - 1)

                // Do something with the previous value after the change
                $("#add" + e.target.id.slice(-1)).nextAll().remove();
                switch (this.value) {
                    case "COUNTIF":
                    case "SUMIF":
                        $(this).parent().append(
                            "<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                            number + "'>");
                        break;
                    case "LARGE":
                    case "SMALL":
                        $(this).parent().append(
                            "<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                            number + "'>");
                        break;
                    case "SUBTOTAL":
                        let tempString = "<select class='' id='aggrePara" + number +
                            "'><option value='' disabled selected hidden>Function_num</option>";
                        for (let i = 0; i < subtotalFunc.length; i++) {
                            tempString +=
                                "<option value='" + (i + 1) + "''>" + subtotalFunc[i] + "</option>";
                        }
                        tempString += "</select>";
                        $(this).parent().append(tempString);
                        break;
                    case "RANK":
                        let tempString1 = "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                            number + "'>";
                        tempString1 +=
                            "<select class='' id='aggrePara" + number + number +
                            "'><option value='0' selected >ascending</option><option value='1'>descending</option></select>"
                        $(this).parent().append(tempString1);
                        break;
                }
            });


        } else {
            alert("You have add too many options.")
        }

    }
);

// hierarchical formula builder: for each line
function createAggreString(specificId) {
    let targetId = specificId ? specificId : aggregateTotalNum;
    let tempString = "<div id='line" + targetId + "'><i class=\"fa fa-minus-circle fa-1x hierRemove\" style=\"color: #ff6b6b;\" id='rm" + targetId + "' aria-hidden=\"true\"></i><select class='custom-select my-1' id='aggregateCol" +
        targetId +
        "''><option value='' disabled selected hidden>Attribute" + "</option>";
    if (targetId == 0) {
        for (let i = 0; i < options.length; i++) {
            aggregateColStr +=
                "<option value='" + (i + 1) + "''>" + options[i] + "</option>";
        }
        aggregateColStr += "</select>";
        tempString += aggregateColStr;
    } else {
        tempString += aggregateColStr;
    }

    tempString += "<select class='custom-select my-1 ' id='aggregateOpt" +
        targetId +
        "''><option value='' disabled selected hidden>Function" +
        "</option>";
    if (targetId == 0) {
        for (let i = 0; i < funcOptions.length; i++) {
            aggregateOptStr += "<option value='" + funcOptions[i] + "''>" +
                funcOptions[i] + "</option>";
        }
        aggregateOptStr += "</select>";
        tempString += aggregateOptStr;
    } else {
        tempString += aggregateOptStr;
    }
    tempString += "<i class=\"fa fa-plus-circle fa-1x hierAdd\" style=\"color: #20c997;\" id='add" + targetId + "' aria-hidden=\"true\"></i></div>";
    aggregateTotalNum += 1;
    return tempString;
}


// create sorting html code: for each line
function createSortString() {
    let tempString =
        "<label class='my-1 mr-5' for='inlineOpt" + sortTotalNum +
        "'>Attribute</label><select class='custom-select my-1 mr-xl-5' id='inlineOpt" +
        sortTotalNum + "''> ";
    if (sortTotalNum == 0) {
        for (let i = 0; i < options.length; i++) {
            sortOptionString +=
                "<option value='" + (i + 1) + "'>" + options[i] + "</option>";
        }
        sortOptionString += "</select><br>";
        tempString += sortOptionString;
    } else {
        tempString += sortOptionString;
    }
    sortTotalNum += 1;
    return tempString;
}

// adding a new attribute for sort pop-up menue
$("#sortAdd").click(function () {
    var $sortDropdown = $("#inlineOpt");
    $sortDropdown.append(createSortString());
})

$("#sortRemove").click(function () {
    if (sortTotalNum > 1) {
        $("#inlineOpt :last-child").remove();
        $("#inlineOpt :last-child").remove();
        sortTotalNum -= 1;
    }
})

// handle exploration form submit
$("#explore-form").submit(function (e) {
    e.preventDefault();
    exploreAttr = $('input[name=exploreValue]:checked').val();
    if (exploreAttr !== undefined) {
        $("#exploration-bar").css("display", "none");
        exploreOpen = true;
        Explore(exploreAttr);
    }
});

// handle exploration form and hierarchi-form close
$(".formClose").click(function (e) {
    this.parentNode.parentNode.style.display = 'none';
    if (exploreOpen) {
        hot.updateSettings({
            width: $("#test-hot").width() + wrapperWidth * 0.19,
        });
    } else {
        hot.updateSettings({
            width: $('.wrapper').width() * 0.99,
        });
    }
})

var dataBucket = [[0, 50], [51, 100], [101, 150], [151, 200], [201, 300], [301, 600], [601, 1500]];
// Customize Bucket start
$("#Bucket").click(function () {
    $("#exploration-bar").css("display", "none");
    $("#hierarchical-col").css("display", "none");
    let originalWidth = wrapperWidth - $("#navChart").width();
    let newWidth = originalWidth - wrapperWidth * 0.19;
    if (newWidth < 0) newWidth = 0;
    hot.updateSettings({width: newWidth});
    $("#bucket-col").css({
        "float": "left",
        "width": wrapperWidth * 0.19,
        "height": wrapperHeight * 0.95,
        "display": "inline"
    });
    let queryData = {};
    let childlist = computePath();
    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = childlist;

    $.ajax({
        url: baseUrl + "redefineBoundaries",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        console.log("Print BE Buckets");
        console.log(e);
        if (e.status == "success") {
            isBucketNumeric = e.data.isNumeric;
            if (isBucketNumeric) {
                $("#textBucket").css("display", "none");
                dataBucket = [];
                for (let i = 0; i < e.data.bucketArray.length; i++) {
                    let temp = [];
                    temp.push(e.data.bucketArray[i][0]);
                    temp.push(parseFloat(e.data.bucketArray[i][1]));
                    dataBucket.push(temp);
                }
                var $buckets = $("#bucketOpt");
                $buckets.empty();
                console.log(dataBucket)
                let tempString = "<div id='bucket" + 0 + "'>"
                    + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
                    + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][1] + " >"
                    + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                    + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
                for (let i = 1; i < dataBucket.length; i++) {
                    tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                        + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value =" + dataBucket[i][0] + " >"
                        + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value =" + dataBucket[i][1] + " >"
                        + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                        + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
                }
                $buckets.append(tempString);
            } else {
                // fot text based data
                $("#bucketAll").prop("checked", false);
                $("#textBucket").css("display", "block");
                dataBucket = [];
                dataBucket = e.data.bucketArray;
                var $buckets = $("#bucketOpt");
                $buckets.empty();
                console.log(dataBucket)
                let tempString = "<div id='bucket" + 0 + "'>"
                    + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
                    + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value ='" + dataBucket[0][dataBucket[0].length - 1] + "' readonly>"
                    + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                    + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
                for (let i = 1; i < dataBucket.length; i++) {
                    tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                        + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                        + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value ='" + dataBucket[i][dataBucket[i].length - 1] + "' readonly>"
                        + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                        + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
                }
                $buckets.append(tempString);


            }
        }
    })
});

$(document).on("click", ".bucket-add", function (e) {
    $("#bucketAll").prop("checked", false);
    let line = Number(e.target.id.substring(9));
    if (isBucketNumeric) {
        let newupp = dataBucket[line][1];
        let oldlower = isNaN(dataBucket[line][0]) ? parseFloat(dataBucket[line][0].slice(0, -1)) : +dataBucket[line][0];
        if (newupp - oldlower < 1) {
            alert("You cannot split further");
            return;
        }
        dataBucket[line][1] = parseFloat(((newupp - oldlower) / 2 + oldlower).toFixed(2));
        let newBucket = [dataBucket[line][1] + "+", newupp];
        dataBucket.splice(line + 1, 0, newBucket);

        var $buckets = $("#bucketOpt");
        $buckets.empty();
        console.log(dataBucket)
        let tempString = "<div id='bucket" + 0 + "'>"
            + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
            + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][1] + " >"
            + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
            + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
        for (let i = 1; i < dataBucket.length; i++) {
            tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value =" + dataBucket[i][0] + " >"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value =" + dataBucket[i][1] + " >"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
        }
        $buckets.append(tempString);
    } else {
        let len = dataBucket[line].length;
        if (len == 1) {
            alert("You cannot split further");
            return;
        }
        len = Math.ceil(len / 2);
        let newBucket = dataBucket[line].splice(len);
        dataBucket.splice(line + 1, 0, newBucket);
        var $buckets = $("#bucketOpt");
        $buckets.empty();
        console.log(dataBucket)
        let tempString = "<div id='bucket" + 0 + "'>"
            + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
            + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value ='" + dataBucket[0][dataBucket[0].length - 1] + "' readonly>"
            + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
            + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
        for (let i = 1; i < dataBucket.length; i++) {
            tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value ='" + dataBucket[i][dataBucket[i].length - 1] + "' readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
        }
        $buckets.append(tempString);

    }

});

$(document).on("click", ".bucket-rm", function (e) {
    let line = Number(e.target.id.substring(8));
    if (isBucketNumeric) {
        let newupp = dataBucket[line][1];
        dataBucket[line - 1][1] = newupp;
        dataBucket.splice(line, 1,);

        var $buckets = $("#bucketOpt");
        $buckets.empty();
        console.log(dataBucket)
        let tempString = "<div id='bucket" + 0 + "'>"
            + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
            + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][1] + " >"
            + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
            + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
        for (let i = 1; i < dataBucket.length; i++) {
            tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value =" + dataBucket[i][0] + " >"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value =" + dataBucket[i][1] + " >"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
        }
        $buckets.append(tempString);
    } else {
        let temp = dataBucket[line];
        for (let i = 0; i < temp.length; i++) {
            dataBucket[line - 1].push(temp[i]);
        }
        dataBucket.splice(line, 1,);
        var $buckets = $("#bucketOpt");
        $buckets.empty();
        console.log(dataBucket)
        let tempString = "<div id='bucket" + 0 + "'>"
            + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
            + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value ='" + dataBucket[0][dataBucket[0].length - 1] + "' readonly>"
            + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
            + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
        for (let i = 1; i < dataBucket.length; i++) {
            tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value ='" + dataBucket[i][dataBucket[i].length - 1] + "' readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
        }
        $buckets.append(tempString);
    }


});

$(document).on("click", ".bucket-multiAdd", function (e) {
    let line = Number(e.target.id.substring(12));
    let len = dataBucket[line].length;
    if (len == 1) {
        alert("You cannot split further!");
        return;
    }
    $(this).nextAll().remove();
    let tempString = "";
    if (isBucketNumeric) {
        tempString += "<br><span style=\"margin-left:2em;\">No. of Buckets&nbsp</span><input class='multibuck' type='text' name='' id='multibuck" + line + "' >";
    } else {
        tempString += "<br><span style=\"margin-left:2em;\">No. of Buckets&nbsp</span><input class='multibuck' placeholder='Max"
            + len + "' type='text' name='' id='multibuck" + line + "' >";
    }
    tempString += "<i class=\"fa fa-check fa-1x bucket-multiAddSub\" style=\"color: #33fa24;\" id='multibuckSub" + line + "' aria-hidden=\"true\"></i></div>"
        + "<i class=\"fa fa-times fa-1x bucket-multiAddCan\" style=\"color: #fa1426;\" id='multibuckCancel" + line + "' aria-hidden=\"true\"></i></div>";
    $(this).parent().append(tempString);
});

$(document).on("click", ".bucket-multiAddSub", function (e) {
    let line = Number(e.target.id.substring(12));
    let targetValue = $("#multibuck" + line).val();
    if (targetValue == '') {
        alert("You have to input the number of buckets");
        return;
    }
    if (isBucketNumeric) {
        let oldlower = isNaN(dataBucket[line][0]) ? parseFloat(dataBucket[line][0].slice(0, -1)) : +dataBucket[line][0];
        if (targetValue >= 15 || targetValue >= (dataBucket[line][1] - oldlower)) {
            alert("The number of buckets specified is too many");
        } else {
            $("#bucketAll").prop("checked", false);
            let indivisualSize = parseFloat(((dataBucket[line][1] - oldlower) / targetValue).toFixed(2));
            let last = dataBucket[line][1];
            let front = dataBucket[line][0];
            dataBucket.splice(line, 1,);
            dataBucket.splice(line, 0, [front, parseFloat((oldlower + indivisualSize).toFixed(2))]);
            front = oldlower + indivisualSize;
            for (let i = 1; i < targetValue - 1; i++) {
                dataBucket.splice(line + i, 0, [front.toFixed(2) + "+", parseFloat((front + indivisualSize).toFixed(2))]);
                front += indivisualSize;
                console.log(front);
            }

            dataBucket.splice(line + Number(targetValue) - 1, 0, [front.toFixed(2) + "+", last]);
            console.log(dataBucket)
            var $buckets = $("#bucketOpt");
            $buckets.empty();
            console.log(dataBucket)
            let tempString = "<div id='bucket" + 0 + "'>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][1] + " >"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
            for (let i = 1; i < dataBucket.length; i++) {
                tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                    + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value =" + dataBucket[i][0] + " >"
                    + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value =" + dataBucket[i][1] + " >"
                    + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                    + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
            }
            $buckets.append(tempString);
        }
    } else {
        if (targetValue > dataBucket[line].length) {
            alert("The number of buckets specified is too many");
        } else {
            $("#bucketAll").prop("checked", false);
            let size = Math.floor(dataBucket[line].length / targetValue);
            for (let i = 0; i < targetValue - 1; i++) {
                let temp = dataBucket[line + i].splice(size);
                dataBucket.splice(line + i + 1, 0, temp);
            }
            console.log(dataBucket);
            var $buckets = $("#bucketOpt");
            $buckets.empty();
            console.log(dataBucket)
            let tempString = "<div id='bucket" + 0 + "'>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value ='" + dataBucket[0][dataBucket[0].length - 1] + "' readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
            for (let i = 1; i < dataBucket.length; i++) {
                tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                    + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                    + "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value ='" + dataBucket[i][dataBucket[i].length - 1] + "' readonly>"
                    + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                    + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
            }
            $buckets.append(tempString);
        }
    }

});

$(document).on("click", ".bucket-multiAddCan", function (e) {
    let line = Number(e.target.id.substring(15));
    $("#bucketMulAdd" + line).nextAll().remove();
});

$(document).on("change", ".custom-bucket", function (e) {
    if (e.target.id.includes("bucketlower")) {
        let line = Number(e.target.id.substring(11));
        let prevLow = isNaN(dataBucket[line - 1][0]) ? parseFloat(dataBucket[line - 1][0].slice(0, -1)) : +dataBucket[line - 1][0];
        if (e.target.value > prevLow && e.target.value < dataBucket[line][1]) {
            dataBucket[line - 1][1] = e.target.value;
            $("#bucketupper" + (line - 1)).val(dataBucket[line - 1][1]);
            dataBucket[line][0] = e.target.value + "+";
            $("#bucketlower" + line).val(dataBucket[line][0]);

        } else {
            alert("The modified lower range is too high or too low");
            $("#bucketlower" + line).val(dataBucket[line][0]);
            $("#bucketupper" + (line - 1)).val(dataBucket[line - 1][1]);
            e.preventDefault();
        }
    } else if (e.target.id.includes("bucketupper")) {
        let line = Number(e.target.id.substring(11));
        if (line == (dataBucket.length - 1)) {
            alert("You cannot modify the terminating point");
            $("#bucketupper" + line).val(dataBucket[line][1]);
            e.preventDefault();
            return;
        }
        let currLow = isNaN(dataBucket[line][0]) ? parseFloat(dataBucket[line][0].slice(0, -1)) : +dataBucket[line][0];
        if (e.target.value < dataBucket[line + 1][1] && e.target.value > currLow) {
            dataBucket[line + 1][0] = e.target.value + "+";
            $("#bucketlower" + (line + 1)).val(dataBucket[line + 1][0]);
            dataBucket[line][1] = e.target.value;
        } else {
            alert("The modified upper range is too high");
            $("#bucketupper" + line).val(dataBucket[line][1]);
            $("#bucketlower" + (line + 1)).val(dataBucket[line + 1][0]);
            e.preventDefault();
        }

    }
});

$(document).on("input", ".custom-bucket", function (e) {
    let line = Number(e.target.id.substring(11));
    if (e.target.id.includes("bucketlower")) {
        $("#bucketupper" + (line - 1)).val(e.target.value);
    } else if (e.target.id.includes("bucketupper") && line < (dataBucket.length - 1)) {
        $("#bucketlower" + (line + 1)).val(e.target.value + "+");
    }
});

$("#bucketAll").click(function (e) {
    if ($(this).prop("checked")) {
        if (isBucketNumeric) {
            var $buckets = $("#bucketOpt");
            $buckets.empty();
            let lower = dataBucket[0][0];
            let upper = dataBucket[dataBucket.length - 1][1]
            dataBucket = [[lower, upper]];
            let tempString = "";
            tempString += "<div id='bucket" + 0 + "'>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][1] + " readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
            $buckets.append(tempString);
        } else {
            $("#displayAll").prop("checked", false);
            var $buckets = $("#bucketOpt");
            $buckets.empty();
            let temp = [];
            for (let i = 0; i < dataBucket.length; i++) {
                for (let j = 0; j < dataBucket[i].length; j++) {
                    temp.push(dataBucket[i][j]);
                }
            }
            dataBucket = [temp];
            let tempString = "";
            tempString += "<div id='bucket" + 0 + "'>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value =" + dataBucket[0][0] + " readonly>"
                + "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value =" + dataBucket[0][dataBucket[0].length - 1] + " readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
            $buckets.append(tempString);
        }

    }
});

$("#displayAll").click(function (e) {
    if ($(this).prop("checked")) {
        $("#bucketAll").prop("checked", false);
        let temp = [];
        for (let i = 0; i < dataBucket.length; i++) {
            for (let j = 0; j < dataBucket[i].length; j++) {
                temp.push([dataBucket[i][j]]);
            }
        }
        dataBucket = temp;
        var $buckets = $("#bucketOpt");
        $buckets.empty();
        console.log(dataBucket)
        let tempString = "<div id='bucket" + 0 + "'>"
            + "<input type='text' class='custom-bucket ' id='bucketlower" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
            //+ "<input type='text' class='custom-bucket ' id='bucketupper" + 0 + "' value ='" + dataBucket[0][0] + "' readonly>"
            + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + 0 + "' aria-hidden=\"true\"></i>"
            + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd0' aria-hidden=\"true\"></i></div>";
        for (let i = 1; i < dataBucket.length; i++) {
            tempString += "<div id='bucket" + i + "'><i class=\"fa fa-minus-circle fa-1x bucket-rm\" style=\"color: #74a7fa;\" id='bucketRm" + i + "' aria-hidden=\"true\"></i>"
                + "<input type='text' class='custom-bucket ' id='bucketlower" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                //+ "<input type='text' class='custom-bucket ' id='bucketupper" + i + "' value ='" + dataBucket[i][0] + "' readonly>"
                + "<i class=\"fa fa-plus-circle fa-1x bucket-add\" style=\"color: #74a7fa;\" id='bucketAdd" + i + "' aria-hidden=\"true\"></i>"
                + "\<i class=\"fa fa-angle-double-down fa-1x bucket-multiAdd\" style=\"color: #74a7fa;\" id='bucketMulAdd" + i + "' aria-hidden=\"true\"></i></div>";
        }
        $buckets.append(tempString);
    }
});

// $('#bucket-form').on('keyup keypress', function(e) {
//
//     var keyCode = e.keyCode || e.which;
//     if (keyCode === 13) {
//         e.preventDefault();
//         return;
//     }
// });

$("#bucket-form").submit(function (e) {
    e.preventDefault();
    let queryData = {};
    let childlist = computePath();
    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = childlist;
    if (isBucketNumeric) {
        queryData.bucketArray = dataBucket;
    } else {
        let temp = [];
        for (let i = 0; i < dataBucket.length; i++) {
            temp.push([dataBucket[i][0], dataBucket[i][dataBucket[i].length - 1]]);
        }
        queryData.bucketArray = temp;
    }

    $.ajax({
        url: baseUrl + "updateBoundaries",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        if (e.status == "success") {
            $("#bucket-col").css("display", "none");
            hot.updateSettings({width: wrapperWidth * 0.79});
            if (currLevel > 0) {
                let targetChild = levelList[levelList.length - 1];
                //zoomOutHist(nav);
                //zoomIn(targetChild, nav);
                jumpToHistorialView(childlist);
            } else {
                Explore(exploreAttr);
            }

        }

    });

});


// navigation start, showing left column.
function Explore(e) {
    $("#navPath")
        .css({"display": "block", "height": "5%"}); // add the breadcrumb
    $("#Hierarchical").click(function () { // handling hierarchical column click in
        // the Exploration Tools
        $("#exploration-bar").css("display", "none");
        $("#bucket-col").css("display", "none");
        //hot.updateSettings({width: wrapperWidth * 0.59});
        let originalWidth = wrapperWidth - $("#navChart").width();
        let newWidth = originalWidth - wrapperWidth * 0.19;
        if (newWidth < 0) newWidth = 0;
        hot.updateSettings({width: newWidth});
        $("#hierarchical-col").css({
            "float": "left",
            "width": wrapperWidth * 0.19,
            "height": wrapperHeight * 0.95,
            "display": "inline"
        });
    });

    $("#exploretoolDropdown").click(function (event) {
        var selectedArray = nav.getSelected();
        if (selectedArray && selectedArray.length == 1) {
            $("#Sort").css({"display": "block"})
        } else {
            $("#Sort").css({"display": "none"})
        }
        if (exploreOpen) {
            $("#Bucket").css({"display": "block"})
        } else {
            $("#Bucket").css({"display": "none"})
        }
    })

    $("#test-hot").css({"float": "left"});
    $("#navChart").css({"display": "inline", "float": "left"});

    $("#history-option").empty();

    $.get(baseUrl + 'startNav/' + bId + '/' + sName + '/' + e, function (data) {

        selectedChild = [];
        selectedChild.push(0);

        selectedBars = [];
        let barObj = {};
        barObj.cell = 0;
        barObj.bars = [0];
        selectedBars.push(barObj);

        childHash = new Map();
        clickable = true;
        currLevel = 0;
        levelList = [];
        spanList = [];
        cumulativeData = [];

        mergeCellInfo = [];
        colHeader = [options[e - 1]];
        cumulativeDataSize = 0;

        currData = data.data;
        for(let i=0;i<currData.length;i++)
        {
            childHash.set(i,currData[i].children);
        }
        //console.log("startNav currdata");
        //console.log(currData);
        //console.log(childHash);
        currRange =
            currData[currData.length - 1].rowRange[1] - currData[0].rowRange[0];

        cumulativeData.push(currData);
        viewData = new Array(currData.length);
        // for (let i = 0; i < currData.length; i++){
        //   viewData[mergeCellInfo[i].row][currLevel]= cumulativeData[0][i].name;
        // }
        for (let i = 0; i < currData.length; i++) {
            viewData[i] = [""];
        }
        for (let i = 0; i < currData.length; i++) {
            viewData[i][0] = cumulativeData[0][i].name;
        }

        console.log(viewData);

        cumulativeDataSize += currData.length;

        hot.updateSettings({
            width: wrapperWidth * 0.79,
            height: wrapperHeight * 0.95,
        });

        // default setting
        var navSettings = {
            // minRows: currData.length,
            //   maxRows:11,
            minCols: 1,
            // maxCols:1,
            // autoColumnSize : true,
            readOnly: true,
            rowHeights: (wrapperHeight * 0.95 / currData.length > 90)
                ? wrapperHeight * 0.95 / currData.length
                : 90,
            // startRows: 200,
            //  startCols: 5,
            width: wrapperWidth * 0.19,
            height: wrapperHeight * 0.95,
            rowHeaderWidth: 0,
            rowHeaders: true,
            colWidths: function (col) {
                if (currLevel == 0) {
                    if (col == 0) {
                        return wrapperWidth * 0.18;
                    } else {
                        return wrapperWidth * 0.15;
                    }
                } else {
                    if (col == 0) {
                        return wrapperWidth * 0.04;
                    } else {
                        return wrapperWidth * 0.15;
                    }
                }

            },
            colHeaders: function (col) {
                if (col < colHeader.length) {
                    if (currLevel == 0) {
                        switch (col) {
                            case 0:
                                return colHeader[0];
                            default:
                                let check =
                                    aggregateData.formula_ls[col - 1].getChart ? "checked" : "";
                                return colHeader[col] + "<span id='colClose' >x</span>" +
                                    "<label class=\"switch\">" +
                                    "  <input type=\"checkbox\"" + check + ">" +
                                    "  <span class=\"slider round\"></span>" +
                                    "</label>";
                        }
                    } else {
                        switch (col) {
                            case 0:
                                return colHeader[0];
                            case 1:
                                return colHeader[1];
                            default:
                                let check =
                                    aggregateData.formula_ls[col - 2].getChart ? "checked" : "";
                                return colHeader[col] + "<span id='colClose'>x</span>" +
                                    "<label class=\"switch\">" +
                                    "  <input type=\"checkbox\"" + check + ">" +
                                    "  <span class=\"slider round\"></span>" +
                                    "</label>";
                        }
                    }
                }
            },
            stretchH: 'all',
            contextMenu: false,
            outsideClickDeselects: false,
            //className: " wrap",
            className: "wrap",
            search: true,
            sortIndicator: true,
            manualColumnResize: true,
            mergeCells: mergeCellInfo,

            beforeOnCellMouseDown: function (e, coords, element) {
                $("#formulaBar").val("");

                let topLevel = (currLevel == 0 && coords.col != 0)
                let otherLevel = (currLevel > 0 && coords.col != 1)
                if (topLevel && coords.row >= 0) {
                    $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 1]);
                }
                else if (currLevel > 0 && coords.row >= 0 && coords.col >= 2) {
                    $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 2]);
                }
                console.log(e);
                if (topLevel || otherLevel || zoomming ||
                    e.realTarget.className == "colHeader" ||
                    e.realTarget.className == "relative" || e.realTarget.className.baseVal == "bar") {
                    e.stopImmediatePropagation();
                }
                if (e.realTarget.classList['3'] == "zoomInPlus") {
                    e.stopImmediatePropagation();
                    zoomIn(coords.row, nav);
                }
                if (e.realTarget.classList['3'] == "zoomOutM") {
                    e.stopImmediatePropagation();
                    zoomOutHist(nav);
                    return;
                }
                if (e.realTarget.id == "colClose") {
                    removeHierarchiCol(coords.col)
                }
                if (e.realTarget.classList['0'] == "slider") {
                    let level = coords.col - 1;
                    if (currLevel > 0)
                        level = coords.col - 2;
                    aggregateData.formula_ls[level].getChart =
                        !aggregateData.formula_ls[level].getChart;
                    getAggregateValue();
                }
            },
            afterSelection: function (r, c, r2, c2, preventScrolling,
                                      selectionLayerLevel) {
                // setting if prevent scrolling after selection

                if (cumulativeData[currLevel][r] != undefined) {
                    selectedChild = [];
                    selectedChild.push(r);

                    selectedBars = [];
                    let barObj = {};
                    barObj.cell = r;
                    barObj.bars = [0];
                    selectedBars.push(barObj);

                    lowerRange = cumulativeData[currLevel][r].rowRange[0];
                    upperRange = cumulativeData[currLevel][r].rowRange[1];
                    updateData(cumulativeData[currLevel][r].rowRange[0], 0,
                        cumulativeData[currLevel][r].rowRange[1], 15, true);
                    updataHighlight();
                    nav.render();
                }
            },

            data: viewData,
            cells: function (row, column, prop) {
                let cellMeta = {}
                if (currLevel == 0) {
                    if (column == 0) {
                        cellMeta.renderer = navCellRenderer;
                    } else {
                        cellMeta.renderer = chartRenderer;
                    }
                }
                else {
                    if (column <= 1) {
                        cellMeta.renderer = navCellRenderer;
                    } else {
                        cellMeta.renderer = chartRenderer;
                    }
                }
                return cellMeta;
            }
        }
        // //initializing interface
        navContainer.innerHTML = ""
        nav = new Handsontable(navContainer, navSettings);
        //nav.selectCell(0, 0);

        console.log("dsa");

        updateData(0, 0, 1000, 15, true);
        lowerRange = 0;
        upperRange = 1000;
        updataHighlight();

        $("#navChart").resizable({handles: 'e'});
// The resizing will also invoke the $(window).resize function in index.js
        $('#navChart').resize(function () {
            let leftWidth = $("#navChart").width();
            console.log(leftWidth);
            let rightWidth = wrapperWidth * 0.98 - leftWidth;
            if (rightWidth < 0) rightWidth = 0;
            // nav.render();
            nav.updateSettings({width: leftWidth});
            $('#test-hot').width(rightWidth);
        });

        //   doubleclick implementation option2:
        nav.view.wt.update('onCellDblClick', function (e, cell) {
            console.log(e);
            if (cell.row >= 0) {
                if (currLevel == 0) {
                    if (cell.col == 0 && cumulativeData[currLevel][cell.row].clickable) {
                        //        var child = cell.row/spanList[currLevel];
                        var child = cell.row;
                        nav.deselectCell();
                        zoomming = true;
                        zoomIn(child, nav);
                    }
                } else {
                    if (cell.col == 1 && cumulativeData[currLevel][cell.row].clickable) {
                        //  var child = cell.row/spanList[currLevel];
                        var child = cell.row;
                        nav.deselectCell();
                        zoomming = true;
                        zoomIn(child, nav);
                    } else if (cell.col == 0) {
                        zoomouting = true;
                        zoomOutHist(nav);
                    }
                }
            }
        });
    });
}

var childHash = new Map();

function computeCellChart(chartString, row,) {

    let result = childHash.get(row);
    let number = result.length;
    let maxLen = 0;
    let hash = new Map();
    let chartData = [];

    for (let i = 0; i < number; i++) {
        if (result[i].name.length > maxLen) {
            maxLen = result[i].name.length;
        }
        let value;
        if (result[i].name.length > 12) {
            value = result[i].name.substring(0, 13) + "...";
            hash.set(value, {name: result[i].name, range: result[i].rowRange[0]})
        } else {
            value = result[i].name;
            hash.set(value, {name: result[i].name, range: result[i].rowRange[0]});
        }
        chartData.push({name: value, count: result[i].value});
    }

    let maxleft = 75;

    let margin = {top: 0, right: 40, bottom: 5, left: maxleft};
    var fullWidth = currLevel == 0 ? wrapperWidth * 0.18 : wrapperWidth * 0.15;
    var fullHeight = (wrapperHeight * 0.95 / cumulativeData[currLevel].length > 90)
        ? wrapperHeight * 0.95 / cumulativeData[currLevel].length - 10 : 80;
    if (number > 6) {
        fullHeight += (number - 6) * 5;
    }
    var width = fullWidth - margin.right - margin.left;
    var height = fullHeight - margin.top - margin.bottom;
    var svg = d3.select("#" + chartString)
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var x = d3.scaleLinear()
        .range([0, width])
        .domain([0, d3.max(chartData, function (d) {
            return d.count;
        })]);

    var y = d3.scaleBand()
        .rangeRound([0, height])
        .padding(0.1)
        .domain(chartData.map(function (d) {
            return d.name;
        }));

    // //make y axis to show bar names
    var yAxis = d3.axisLeft(y)
        .tickSize(0);

    var tooltip =
        d3.select('#' + chartString).append("div").attr("class", "toolTip");
    var gy = svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .selectAll(".tick text")
        .data(chartData)
        .on("mouseover",
            function (d) {
                //           console.log(d)
                tooltip.style("left", d3.event.pageX - 20 + "px")
                    .style("top", d3.event.pageY - 30 + "px")
                    .style("display", "inline-block")
                    .style("font", "10px")
                    .html(hash.get(d.name).name);
            })
        .on("mouseout", function (d) {
            tooltip.style("display", "none");
        });

    var bars = svg.selectAll(".bar")
        .data(chartData)
        .enter()
        .append("g")

    //append rects
    bars.append("rect")
        .attr("class", "bar")
        .attr("y", function (d) {
            return y(d.name);
        })
        .attr("height", y.bandwidth())
        .attr("x", 0)
        .attr('fill', function (d,i) {
            //console.log("selectedBars");
            //console.log(selectedBars);
            //console.log(row,i);
            for(let ind=0;ind<selectedBars.length;ind++)
            {
                if(selectedBars[ind].cell == row)
                {
                    if(selectedBars[ind].bars.includes(i))
                        return "#32CC99";//'#ff4500';
                    else
                        return "#70DCB8";//'#ffA500';
                }
            }
            return "#70DCB8";//'#ffA500';
        })
        .attr("width", function (d) {
            return x(d.count);
        })
        .style("stroke-width", 1)
        .on("mouseover",
            function (d) {
                //           console.log(d)
                tooltip.style("left", d3.event.pageX - 20 + "px")
                    .style("top", d3.event.pageY - 30 + "px")
                    .style("display", "inline-block")
                    .style("font", "10px")
                    .html(hash.get(d.name).name);
            })
        .on("mouseout", function (d) {
            tooltip.style("display", "none");
        })
        .on("click", function (d) {
            lowerRange = hash.get(d.name).range;
            upperRange = lowerRange + 500;
            updateData(lowerRange, 0, upperRange, 15, true);
            updataHighlight();
        });
    //   .on("dblclick",function(d){ alert("node was double clicked"); });

    //add a value label to the right of each bar
    bars.append("text")
        .attr("class", "label")
        //y position of the label is halfway down the bar
        .attr("y", function (d) {
            return y(d.name) + y.bandwidth() / 2 + 4;
        })
        .style("font-size", "10px")
        //x position is 3 pixels to the right of the bar
        .attr("x", function (d) {
            return x(d.count) + 3;
        })
        .text(function (d) {
            return d.count;
        });

}

function navCellRenderer(instance, td, row, col, prop, value, cellProperties) {
    let tempString = "<div><span>" + value + "</span>";

   // console.log("currLevel in navcellRenderer: "+currLevel);
    // differentiate single layer to double layer
    if (currLevel == 0) {

        if (selectedChild.includes(row)) {
            td.style.background = '#D3D3D3';
            td.style.color = '#4e81d3';
        } else {
            td.style.background = '#F5F5DC';
        }
        //console.log("curr0");
       // console.log(cumulativeData[currLevel]);
        let targetCell = cumulativeData[currLevel][row];

        if (targetCell.clickable) {
            tempString += " (Rows: " + targetCell.value+")";
            tempString += "<i class=\"fa fa-angle-right fa-2x zoomInPlus\" style=\"color: #51cf66;\" id='zm" + row + "' aria-hidden=\"true\"></i>";

            if (childHash.has(row)) {
                let chartString = "navchartdiv" + row + col;
                tempString += "<div id=" + chartString + " ></div>";
                td.innerHTML = tempString + "</div>";
                ///console.log("rerender");
                computeCellChart(chartString, row);
                return;
            }
            /*let queryData = {};
            queryData.bookId = bId;
            queryData.sheetName = sName;
            queryData.path = row.toString();

            $.ajax({
                url: baseUrl + "getChildren",
                method: "POST",
                // dataType: 'json',
                contentType: 'text/plain',
                data: JSON.stringify(queryData),
                async: false,

            }).done(function (e) {
                if (e.status == "success") {
                    console.log("getingchild" + row);
                    let chartString = "navchartdiv" + row + col;
                    tempString += "<div id=" + chartString + " ></div>";
                    td.innerHTML = tempString + "</div>";
                    let result = e.data.buckets;
                    childHash.set(row, result);
                    updateBarChartFocus(currentFirstRow,currentLastRow);
                    computeCellChart(chartString, row);
                }
            })*/
        } else {
            tempString += "<p>Rows: " + targetCell.value + "<br> Start: " + targetCell.rowRange[0] + "<br> End: " + targetCell.rowRange[1] + "</p>";
            td.innerHTML = tempString + "</div>";
            return;
        }

    } else {



        if (col == 1 && selectedChild.includes(row)) {
            td.style.background = '#D3D3D3';
            td.style.color = '#4e81d3';
        }
        else {
            td.style.background = '#F5F5DC';
        }
        if (col == 0) {
            tempString = "<div><i class=\"fa fa-angle-left fa-3x zoomOutM\" style=\"color: #339af0;\" id='zm" + row + "' aria-hidden=\"true\"></i>";
            // tempString += "<span class='vertical'>" + value + "</span>";
            // td.innerHTML = tempString + "</div>";
            let chartString = "parentCol" + row + col;
            tempString += "<div id=" + chartString + " ></div>";
            td.innerHTML = tempString + "</div>";
            let holder = d3.select("#"+chartString)
                .append("svg")
                .attr("width", wrapperWidth * 0.02)
                .attr("height", wrapperHeight * 0.7);
            let yoffset = wrapperHeight* 0.4;
            // draw the text
            holder.append("text")
                .style("fill", "black")
                .style("font-size", "20px")
                .attr("dy", ".35em")
                .attr("text-anchor", "middle")
                .attr("transform", "translate(8,"+yoffset +") rotate(270)")
                .text(value);
            return;
        } else {
            let targetCell = cumulativeData[currLevel][row];
            if (targetCell.clickable) {
                tempString += " (Rows: " + targetCell.value+")";
                tempString += "<i class=\"fa fa-angle-right fa-2x zoomInPlus\" style=\"color: #51cf66;\" id='zm" + row + "' aria-hidden=\"true\"></i>";

                if (childHash.has(row)) {
                    let chartString = "navchartdiv" + row + col;
                    tempString += "<div id=" + chartString + " ></div>";
                    td.innerHTML = tempString + "</div>";
                   // console.log("rerender");
                    computeCellChart(chartString, row);
                    return;
                }
                /*
                let queryData = {};
                queryData.bookId = bId;
                queryData.sheetName = sName;
                queryData.path = computePath() + "," + row;

                $.ajax({
                    url: baseUrl + "getChildren",
                    method: "POST",
                    // dataType: 'json',
                    contentType: 'text/plain',
                    data: JSON.stringify(queryData),
                   async: false,

                }).done(function (e) {
                    if (e.status == "success") {
                        console.log("curr1 grchildrow" + row);
                        let chartString = "navchartdiv" + row + col;
                        tempString += "<div id=" + chartString + " ></div>";
                        td.innerHTML = tempString + "</div>";
                        let result = e.data.buckets;
                        childHash.set(row, result);
                        updateBarChartFocus(currentFirstRow,currentLastRow);
                        computeCellChart(chartString, row);

                    }
                })*/
            } else {
                tempString += "<p>Rows: " + targetCell.value + "<br> Start: " + targetCell.rowRange[0] + "<br> End: " + targetCell.rowRange[1] + "</p>";
                td.innerHTML = tempString + "</div>";
            }
        }

    }


}


function removeHierarchiCol(colIdx) {
    if(hierarchicalColAttr.length==1)
        hierarchicalColAttr = [];
    else{
        let index = hierarchicalColAttr.indexOf(colIdx);
        if (index !== -1) hierarchicalColAttr.splice(index, 1);
    }
    // nav.alter('remove_col', colIdx);
    colHeader.splice(
        colIdx,
        1,
    )
    if (currLevel == 0) {
        aggregateData.formula_ls.splice(
            colIdx - 1,
            1,
        );
        navAggRawData.splice(
            colIdx - 1,
            1,
        );
    }
    else {
        aggregateData.formula_ls.splice(
            colIdx - 2,
            1,
        );
        navAggRawData.splice(
            colIdx - 2,
            1,
        );
    }
    if (aggregateData.formula_ls.length == 0) {
        hieraOpen = false;
        nav.alter('remove_col', colIdx);
        nav.updateSettings({width: wrapperWidth * 0.19,});
    } else {
        nav.alter('remove_col', colIdx);
    }

    updataHighlight();
}

$("#hierarchi-form").submit(function (e) {
    aggregateData.bookId = bId;
    aggregateData.sheetName = sName;
    e.preventDefault();
    aggregateData.formula_ls = [];
    // attr_index = []
    // funcId = []
    hieraOpen = false;
    hierarchicalColAttr = [];
    let getChart = ($("#chartOpt").val() == 2);
    for (let i = 0; i < aggregateTotalNum; i++) {
        let attrIdx = $("#aggregateCol" + i).val();
        hierarchicalColAttr.push(parseInt(attrIdx)-1);
        let funct = $("#aggregateOpt" + i).val();
        let paras = [];
        let para;
        if (attrIdx == null) {
            alert("Please select the attribute");
            return;
        }
        switch (funct) {
            case null:
                alert("Please select the function");
                return;
            case "COUNTIF":
            case "SUMIF":
                para = "\""+$("#aggrePara" + i).val()+"\"";
                if (para == "") {
                    alert("Predicate is empty");
                    return;
                } else {
                    paras = ["", para]
                    aggregateData.formula_ls[i] = {
                        attr_index: attrIdx,
                        function: funct,
                        param_ls: paras,
                        getChart: getChart
                    };
                }
                ;
                break;
            case "LARGE":
            case "SMALL":
                para = "\""+$("#aggrePara" + i).val()+"\"";
                if (para == "") {
                    alert("int value is empty");
                    return;
                } else {
                    paras = ["", para]
                    aggregateData.formula_ls[i] = {
                        attr_index: attrIdx,
                        function: funct,
                        param_ls: paras,
                        getChart: getChart
                    };
                }
                ;
                break;
            case "SUBTOTAL":
                para = "\""+$("#aggrePara" + i).val()+"\"";
                if (para == null) {
                    alert("function ID is empty");
                    return;
                } else {
                    paras = [para, ""]
                    aggregateData.formula_ls[i] = {
                        attr_index: attrIdx,
                        function: funct,
                        param_ls: paras,
                        getChart: getChart
                    };
                }
                ;
                break;
            case "RANK":
                para = "\""+$("#aggrePara" + i).val()+"\"";
                if (para == "") {
                    alert("Rank value is empty");
                    return;
                } else {
                    paras = [para, "", $("#aggrePara" + i + i).val()]
                    aggregateData.formula_ls[i] = {
                        attr_index: attrIdx,
                        function: funct,
                        param_ls: paras,
                        getChart: getChart
                    };
                }
                ;
                break;
            default:
                aggregateData.formula_ls[i] = {
                    attr_index: attrIdx,
                    function: funct,
                    param_ls: [""],
                    getChart: getChart
                };
        }
    }
    //console.log("hierarchicalColAttr");
    //console.log(hierarchicalColAttr);
    getAggregateValue();
});

function getAggregateValue() {
    let childlist = computePath();
    aggregateData.path = " " + childlist;
    //console.log("hierarchical col");
   // console.log(aggregateData);
    $.ajax({
        url: baseUrl + "getHierarchicalAggregateFormula",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(aggregateData),
    }).done(function (e) {
        if (e.status == "success") {
            $("#hierarchical-col").css("display", "none");
            hieraOpen = true;
            if (currLevel == 0) {
                colHeader.splice(
                    1,
                    colHeader.length - 1,
                );
                for (let i = 0; i < viewData.length; i++) {
                    viewData[i].splice(
                        1,
                        viewData[i].length - 1,
                    )
                }
            } else {
                colHeader.splice(
                    2,
                    colHeader.length - 2,
                );
                for (let i = 0; i < viewData.length; i++) {
                    viewData[i].splice(
                        2,
                        viewData[i].length - 2,
                    )
                }
            }
            for (let i = 0; i < e.data.length; i++) {
                let hierCol = aggregateData.formula_ls[i];
                colHeader.push(options[hierCol.attr_index - 1] + " " +
                    hierCol.function + " " + hierCol.param_ls);
            }

            addHierarchiCol(e.data);

            //higlight hierarchical col
            updataHighlight();

        } else {
            alert("There is some problem with the formula: " + e.message);
        }
    })
}

function addHierarchiCol(aggregateValue) {
    navAggRawData = aggregateValue;

    let targetCol = (currLevel == 0) ? 1 : 2;
    navRawFormula = [];
    for (let i = 0; i < cumulativeData[currLevel].length; i++) {
        let formulaRow = [];
        for (let j = 0; j < aggregateValue.length; j++) {
            formulaRow.push(aggregateValue[j][i].formula);
            let text = aggregateValue[j][i].value;
            if (isNaN(text)) {
                viewData[i][targetCol + j] = text;
            } else {
                viewData[i][targetCol + j] = text.toFixed(2);
            }
        }
        navRawFormula.push(formulaRow);
    }
    let columWidth = [];
    if (currLevel >= 1) {
        columWidth = [
            ,
            ,
        ];
    } else {
        columWidth = [
            ,
        ];
    }
    for (let j = 0; j < aggregateValue.length; j++) {
        columWidth.push(wrapperWidth * 0.14);
    }

    let numChild = cumulativeData[currLevel].length;
    let newWidth = wrapperWidth*(0.15 + aggregateValue.length * 0.13);
    nav.updateSettings({
        width: newWidth,
        manualColumnResize: columWidth,
        minCols: 1,
        data: viewData,
        rowHeights: (wrapperHeight * 0.95 / numChild > 90)
            ? wrapperHeight * 0.95 / numChild
            : 90,
        mergeCells: mergeCellInfo,
    });
    hot.updateSettings({width: wrapperWidth - $("#navChart").width()});
    if (zoomming) {
        zoomming = false;
        //nav.selectCell(0, 1);
    }
    if (zoomouting) {
        zoomouting = false;
        if (currLevel == 0) {
            //   nav.selectCell(targetChild, 0)
        } else {
            //  nav.selectCell(targetChild, 1);
        }
    }
}

function computePath() {
    let childlist = "";
    for (let i = 0; i < levelList.length - 1; i++) {
        childlist += levelList[i] + ",";
    }
    if (levelList.length > 0) {
        childlist += levelList[levelList.length - 1];
    }
    return childlist;
}

function zoomIn(child, nav) {
    nav.deselectCell();
    var selectFirstChild = false;
    console.log("In zoom in:"+child);
    //console.log(selectedChild);
    childHash = new Map();
    if(!selectedChild.includes(child) || selectedChild.length==0)
        selectFirstChild = true;
    selectedChild = [];
    selectedBars = [];

    if (currLevel == 0) {
        colHeader.splice(1, 0, "")
    }
    levelList.push(child);
    let childlist = computePath(); // get the list of children

    let queryData = {};

    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = childlist;

    $.ajax({
        url: baseUrl + "getChildren",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        if (e.status == "success") {
            var result = e.data;
            //console.log(result);
            currLevel += 1;
            currData = result.buckets;
            for(let i=0;i<currData.length;i++)
            {
                childHash.set(i,currData[i].children);
            }
            prevPath = result.prev.path;
            nextPath = result.later.path;
            let breadcrum_ls = result.breadCrumb;
            mergeCellInfo = [];
            mergeCellInfo.push(
                {row: 0, col: 0, rowspan: currData.length, colspan: 1});

            viewData = new Array(currData.length);
            for (let i = 0; i < currData.length; i++) {
                if (i == 0) {
                    viewData[i] = [cumulativeData[currLevel - 1][child].name];
                } else {
                    viewData[i] = [""];
                }
            }
            //console.log(viewData);
            cumulativeData.splice(currLevel);
            cumulativeData.push(currData);

            for (let i = 0; i < currData.length; i++) {
                // double layer
                viewData[i][1] = cumulativeData[currLevel][i].name;
            }

            cumulativeDataSize += currData.length;

            let columWidth = [];
            if (currLevel >= 1) {
                // columWidth = [50];
            } else {
                columWidth = 200;
            }
            // nav.render();

            if (hieraOpen) {
                getAggregateValue();

            } else {
                nav.updateSettings({
                    // minRows: currData.length,
                    data: viewData,
                    rowHeights: (wrapperHeight * 0.95 / currData.length > 90)
                        ? wrapperHeight * 0.95 / currData.length
                        : 90,
                    mergeCells: mergeCellInfo,
                });
                zoomming = false;
                //nav.selectCell(0, 1)
            }
            updateNavPath(breadcrum_ls); // calculate breadcrumb
            updateNavCellFocus(currentFirstRow,currentLastRow);
            if(selectFirstChild)
                nav.selectCell(0, 1);
        }
    })
}

// computing the breadcrumb
function updateNavPath(breadcrumb_ls) {
    let $breadcrumbList = $(".breadcrumb");
    $breadcrumbList.empty();
    let tempString = "";
    if (currLevel > 0) {
        tempString = "<li class='breadcrumb-item'><a href='#' id='0'>Home</a></li>";
        for (let i = 0; i < breadcrumb_ls.length - 1; i++) {
            tempString += "<li class='breadcrumb-item'> <a href='#' id='" + (i + 1) +
                "'>" + breadcrumb_ls[i] + "</a></li>";
        }
        tempString += "<li class='breadcrumb-item active' aria-current='page'>" +
            breadcrumb_ls[breadcrumb_ls.length - 1] + "</li>";
    } else {
        tempString = "<li class='breadcrumb-item' aria-current='page'>Home</li>"
    }
    $breadcrumbList.append(tempString);

    // add to navigation history

    let navHistoryPath = "Home";
    for (let j = 0; j < breadcrumb_ls.length; j++) {
        navHistoryPath += " > " + breadcrumb_ls[j];
    }

    navHistoryPathIndex[navHistoryPath] = computePath();

    let child_str = navHistoryPathIndex[navHistoryPath];

    child_str = child_str.substring(0, child_str.lastIndexOf(","));

    for (let j = breadcrumb_ls.length - 2; j >= 0; j--) {
        breadCrumbHistoryPathIndex[j + 1] = child_str;
        child_str = child_str.substring(0, child_str.lastIndexOf(","));
    }
    breadCrumbHistoryPathIndex[0] = "";
    if (currLevel == 0)
        return;

    if (navHistroyTable[navHistoryPath] == undefined) // if new path
    {
        //" onclick="jumpToHistorialView(navHistoryPathIndex[navHistoryPath])""
        $("#history-option")
            .prepend("<a class=\"dropdown-item\" href=\"#\" id=\"" +
                navHistoryPath + "\">" + navHistoryPath + "</a>");

        navHistroyTable[navHistoryPath] = true;
    } else // if existing path, delete from dropdown and prepend
    {
        let temp_ls = [];

        $("#history-option").children().each(function () {
            let idVal = $(this)[0].id;
            if (idVal != navHistoryPath)
                temp_ls.push(idVal);
        });
        $("#history-option").children().remove();
        for (let i = 0; i < temp_ls.length; i++)
            $("#history-option")
                .append("<a class=\"dropdown-item\" href=\"#\" id=\"" + temp_ls[i] +
                    "\">" + temp_ls[i] + "</a>");
        $("#history-option")
            .prepend("<a class=\"dropdown-item\" href=\"#\" id=\"" +
                navHistoryPath + "\">" + navHistoryPath + "</a>");
        navHistroyTable[navHistoryPath] = true;
    }

    $("#history-option a").click(function (e) {
        jumpToHistorialView(navHistoryPathIndex[e.target.id]);
    });

    $(".breadcrumb-item a").click(function (e) {
        jumpToHistorialView(breadCrumbHistoryPathIndex[parseInt(e.target.id)]);
    });
}

function zoomOut(nav) {
    clickable = true;
    nav.deselectCell();

    // api call to /levelList + '.' + child to get currData
    let numChild = cumulativeData[currLevel].length;
    currLevel -= 1;
    cumulativeData.pop();
    targetChild = levelList[levelList.length - 1]
    levelList.pop();
    cumulativeDataSize -= numChild;

    numChild = cumulativeData[currLevel].length;
    viewData = new Array(numChild);
    mergeCellInfo = [];
    if (currLevel > 0) {
        mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});
        for (let i = 0; i < numChild; i++) {
            if (i == 0) {
                viewData[i] =
                    [cumulativeData[currLevel - 1][levelList[currLevel - 1]].name];
            } else {
                viewData[i] = [""];
            }
            viewData[i][1] = cumulativeData[currLevel][i].name;
        }
    } else {
        colHeader.splice(1, 1);
        for (let i = 0; i < numChild; i++) {
            viewData[i] = [cumulativeData[currLevel][i].name];
        }
    }

    let columWidth = [];
    if (currLevel >= 1) {
        columWidth = [40, 160];
    } else {
        columWidth = 200;
    }

    if (hieraOpen) {
        getAggregateValue();

    } else {
        nav.updateSettings({

            data: viewData,
            // rowHeights: (wrapperHeight * 0.95 / numChild > 80)
            //     ? wrapperHeight * 0.95 / numChild
            //     : 80,
            mergeCells: mergeCellInfo,
        });
        zoomouting = false;
        // if (currLevel == 0) {
        //     nav.selectCell(targetChild, 0)
        // } else {
        //     nav.selectCell(targetChild, 1);
        // }
    }

    updateNavPath();

    // nav.render();
}

function zoomOutHist(nav) {
    console.log("In Zoom Out");
    childHash = new Map();
    clickable = true;
    nav.deselectCell();
    if (currLevel == 0) {
        colHeader.splice(1, 0, "")
    }
    targetChild = levelList[levelList.length - 1];
    selectedChild = [];
    selectedBars = [];

    levelList.pop();
    let childlist = computePath(); // get the list of children
    // api call to /levelList + '.' + child to get currData
    let queryData = {};

    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = childlist;

    $.ajax({
        url: baseUrl + "getChildren",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        if (e.status == "success") {
            let result = e.data;
            //console.log(result);
            let breadcrum_ls = result.breadCrumb;
            // clickable = result.clickable;
            currLevel = breadcrum_ls.length;
            currData = result.buckets;
            for(let i=0;i<currData.length;i++)
            {
                childHash.set(i,currData[i].children);
            }
            prevPath = result.prev.path;
            nextPath = result.later.path;
            let numChild = currData.length;
            viewData = new Array(numChild);
            cumulativeData[currLevel] = currData;
            mergeCellInfo = [];
            if (currLevel > 0) {
                mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});

                let breadCrumb = result.breadCrumb;
                let parentName = breadCrumb[breadCrumb.length - 1];

                for (let i = 0; i < numChild; i++) {
                    if (i == 0) {
                        viewData[i] = [parentName];
                    } else {
                        viewData[i] = [""];
                    }
                    viewData[i][1] = currData[i].name;
                }
            } else {
                colHeader.splice(1, 1);
                for (let i = 0; i < numChild; i++) {
                    viewData[i] = [currData[i].name];
                }
            }
            //console.log(viewData);
            let columWidth = [];
            if (currLevel >= 1) {
                columWidth = [40, 160];
            } else {
                columWidth = 200;
            }

            if (hieraOpen) {
                getAggregateValue();

            } else {
                nav.updateSettings({

                    data: viewData,
                    rowHeights: (wrapperHeight * 0.95 / numChild > 90)
                        ? wrapperHeight * 0.95 / numChild
                        : 90,
                    mergeCells: mergeCellInfo,
                });
                zoomouting = false;
                nav.render();
                // if (currLevel == 0) {
                //     nav.selectCell(targetChild, 0)
                // } else {
                //     nav.selectCell(targetChild, 1);
                // }
            }

            updateNavPath(breadcrum_ls);
            updateNavCellFocus(currentFirstRow,currentLastRow);
        }
    })
}

function jumpToHistorialView(childlist) {
    console.log("In Jump to Historical view");
    childHash = new Map();
    clickable = true;
    nav.deselectCell();

    let tmp_ls = childlist.split(",");
    levelList = []
    for (let i = 0; i < tmp_ls.length; i++) {
        if (tmp_ls[i].length != 0)
            levelList[i] = parseInt(tmp_ls[i]);
    }
    targetChild = levelList[levelList.length - 1];

    selectedChild = [];
    selectedBars = [];
    // api call to /levelList + '.' + child to get currData
    let queryData = {};

    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = childlist;

    $.ajax({
        url: baseUrl + "getChildren",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        if (e.status == "success") {
            var result = e.data;
            let breadcrumb_ls = result.breadCrumb;
            // clickable = result.clickable;
            currLevel = breadcrumb_ls.length;
            currData = result.buckets;
            for(let i=0;i<currData.length;i++)
            {
                childHash.set(i,currData[i].children);
            }
            prevPath = result.prev.path;
            nextPath = result.later.path;
            let numChild = currData.length;
            viewData = new Array(numChild);
            cumulativeData[currLevel] = currData;
            mergeCellInfo = [];
            if (breadcrumb_ls.length != 0) {
                mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});

                let parentName = breadcrumb_ls[breadcrumb_ls.length - 1];

                for (let i = 0; i < numChild; i++) {
                    if (i == 0) {
                        viewData[i] = [parentName];
                    } else {
                        viewData[i] = [""];
                    }
                    viewData[i][1] = currData[i].name;
                }
            } else {
                colHeader.splice(1, 1);
                for (let i = 0; i < numChild; i++) {
                    viewData[i] = [currData[i].name];
                }
            }

            let columWidth = [];
            if (breadcrumb_ls.length >= 1) {
                columWidth = [40, 160];
            } else {
                columWidth = 200;
            }

            if (hieraOpen) {
                getAggregateValue();

            } else {
                nav.updateSettings({

                    data: viewData,
                    rowHeights: (wrapperHeight * 0.95 / numChild > 90)
                        ? wrapperHeight * 0.95 / numChild
                        : 90,
                    mergeCells: mergeCellInfo,
                });
                zoomouting = false;
                // if (breadcrumb_ls.length == 0) {
                //     nav.selectCell(targetChild, 0)
                // } else {
                //     nav.selectCell(targetChild, 1);
                // }
            }

            updateNavPath(breadcrumb_ls);
            if(currLevel==0) {
                updateNavCellFocus(currentFirstRow,currentLastRow);
            }else
                nav.selectCell(0, 1);
        }
    });
}

$("#sort-form").submit(function (e) {
    e.preventDefault();
    $("#exampleModal").modal('hide')
    sortAttrIndices = [];
    //sortAttrIndices.push(exploreAttr);
    for (let i = 0; i < sortTotalNum; i++) {
        sortAttrIndices.push($('#inlineOpt' + i).val());
    }

    selectedArray = nav.getSelected();
    //  var child = selectedArray[0][0]/spanList[currLevel];
    var child = selectedArray[0][0];
    let childlist = computePath();
    let path = ""
    if (childlist !== "") {
        path += childlist + ',' + child
    }
    else {
        path = child
    }

    let order = 0;
    if (document.getElementById('r2').checked) {
        order = 1;
    }

    $.get(baseUrl + 'sortBlock/' + bId + '/' + sName + '/ ' + path + '/' +
        sortAttrIndices + '/' + order,
        function (data) {
            updateData(cumulativeData[currLevel][child].rowRange[0], 0,
                cumulativeData[currLevel][child].rowRange[1] + 10, 15,
                true)

            updataHighlight(child);
            //updateSScolor(currentFirstRow,currentLastRow);
        });
})

function chartRenderer(instance, td, row, col, prop, value, cellProperties) {
    let colOffset = (currLevel == 0) ? 1 : 2;
    if (navAggRawData[col - colOffset][row].chartType == 0) {
        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";

        let chartData = navAggRawData[col - colOffset][row]['chartData'];
        let distribution = [];

        distribution.push({min: chartData[0], max: chartData[1]});

        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }

        let margin = {top: 20, right: 40, bottom: 18, left: 35};
        // here, we want the full chart to be 700x200, so we determine
        // the width and height by subtracting the margins from those values
        let fullWidth = wrapperWidth * 0.14;
        let fullHeight = nav.getRowHeight(row);

        // the width and height values will be used in the ranges of our scales
        let width = fullWidth - margin.right - margin.left;
        let height = fullHeight;
        let svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform', 'translate(' + margin.left + ',0)');

        svg.append("rect")
            .attr("x", width + margin.right / 4)
            .attr("y", 0)
            .attr("width", margin.right)
            .attr("height", fullHeight)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", (margin.top / 2))
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(value);

        // draw the rectangle
        //'#0099ff'
        let fraction = 3; // what fraction of container is the valuebar
        let valueBarHeight = height / fraction;
        let valueBar = svg.append("rect")
            .attr("x", 0 - margin.left / 2)
            .attr("y", height / 2 - valueBarHeight / 2)
            .attr("width", width)
            .attr("height", valueBarHeight)
            .attr("fill", '#0099ff');
        // add value rectangle
        let xScale = d3.scaleLinear()
            .domain([distribution[0].min, distribution[0].max])
            .range([0 - margin.left / 2, width - margin.left / 2])
            .nice();

        var highlightBar = svg.append("rect")
            .attr("x", xScale(value))
            .attr("y", height / 2 - valueBarHeight)
            .attr("width", 2)
            .attr("height", 2 * valueBarHeight)
            .attr("fill", '#000000');
        // add min, max, value text
        svg.append("text")
            .attr("x", xScale(distribution[0].min) - 5)
            .attr("y", function () {
                return height / 2 + valueBarHeight / 2 + 8;
            })
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(distribution[0].min);
        svg.append("text")
            .attr("x", xScale(distribution[0].max) + 5)
            .attr("y", function () {
                return height / 2 + valueBarHeight / 2 + 8;
            })
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(distribution[0].max);
        svg.append("text")
            .attr("x", xScale(value))
            .attr("y", function () {
                return height / 2 + valueBarHeight + 10;
            })
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(value);
    } else if (navAggRawData[col - colOffset][row].chartType == 1) {
        console.log("in chart rendered");
        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";

        let chartData = navAggRawData[col - colOffset][row]['chartData'];
        let distribution = [];

        for (let i = 0; i < chartData.counts.length; i++) {
            let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
            distribution.push({boundary: boundstr, count: chartData.counts[i]});
        }
        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }

        let special = navAggRawData[col - colOffset][row]['valueIndex'];

        var margin = {top: 20, right: 25, bottom: 18, left: 35};
        // here, we want the full chart to be 700x200, so we determine
        // the width and height by subtracting the margins from those values
        var fullWidth = wrapperWidth * 0.14;
        var fullHeight = nav.getRowHeight(row);

        // the width and height values will be used in the ranges of our scales
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform',
                'translate(' + margin.left + ',' + margin.top + ')');

        svg.append("rect")
            .attr("x", width + margin.right / 4)
            .attr("y", 0 - margin.top)
            .attr("width", margin.right)
            .attr("height", fullHeight)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", 0 - (margin.top / 2))
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(value);

        let xScale =
            d3.scaleLinear()
                .domain([
                    chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                ])
                .range([0, width]);

        // y value determined by temp
        var maxValue = d3.max(distribution, function (d) {
            return d.count;
        });
        var yScale =
            d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

        var xAxis = d3.axisBottom(xScale)
        //.ticks(6,'s');
            .tickValues(chartData.bins);

        var yAxis = d3.axisLeft(yScale);
        yAxis.ticks(5);

        var barHolder = svg.append('g').classed('bar-holder', true);

        var tooltip =
            d3.select('#' + tempString).append("div").attr("class", "toolTip");

        // draw the bars
        var bars =
            barHolder.selectAll('rect.bar')
                .data(distribution)
                .enter()
                .append('rect')
                .classed('bar', true)
                .attr('x',
                    function (d, i) {
                        // the x value is determined using the
                        // month of the datum
                        return 1 + width / (chartData.counts.length) * i;
                    })
                .attr('width', width / (chartData.counts.length))
                .attr('y', function (d) {
                    return yScale(d.count);
                })
                .attr('fill',
                    function (d, i) {
                        if (i == special) {
                            return '#ffA500';
                        } else {
                            return '#0099ff';
                        }
                    })
                .attr('height',
                    function (d) {
                        // the bar's height should align it with the base of the
                        // chart (y=0)
                        return height - yScale(d.count);
                    })
                .on("mouseover",
                    function (d) {
                        tooltip.style("left", d3.event.pageX - 20 + "px")
                            .style("top", d3.event.pageY - 30 + "px")
                            .style("display", "inline-block")
                            .html((d.count));
                    })
                .on("mouseout", function (d) {
                    tooltip.style("display", "none");
                });

        // draw the axes
        svg.append('g')
            .classed('x axis', true)
            .attr('transform', 'translate(0,' + height + ')')
            .call(xAxis);

        var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

        // add a label to the yAxis
        svg.append('text')
            .attr('transform', 'rotate(-90)')
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .style('text-anchor', 'middle')
            .style('fill', 'black')
            .attr('dy', '1em')
            .style('font-size', 10)
            .text('Count');

    } else if (navAggRawData[col - colOffset][row].chartType == 2) {
        console.log("in nav chart renderer");
        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";

        let chartData = navAggRawData[col - colOffset][row]['chartData'];
        let distribution = [];

        for (let i = 0; i < chartData.counts.length; i++) {
            let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
            distribution.push({boundary: boundstr, count: chartData.counts[i]});
        }
        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }

        let avg = chartData.AVERAGE;
        let stdev = chartData.STDEV;

        let showSquare = 1;
        if (navAggRawData[col - colOffset][row]['formula'].includes("STDEV"))
            showSquare = 0;

        var margin = {top: 20, right: 25, bottom: 18, left: 35};
        // here, we want the full chart to be 700x200, so we determine
        // the width and height by subtracting the margins from those values
        var fullWidth = wrapperWidth * 0.14;
        var fullHeight = nav.getRowHeight(row);

        // the width and height values will be used in the ranges of our scales
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform',
                'translate(' + margin.left + ',' + margin.top + ')');

        svg.append("rect")
            .attr("x", width + margin.right / 4)
            .attr("y", 0 - margin.top)
            .attr("width", margin.right)
            .attr("height", fullHeight)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", 0 - (margin.top / 2))
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(function () {
                if (showSquare == 1)
                    return "\u03c3" +
                        "^2: " + value;
                else
                    return "\u03c3" +
                        ": " + value;
            });

        let xScale =
            d3.scaleLinear()
                .domain([
                    chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                ])
                .range([0, width]);

        // y value determined by temp
        var maxValue = d3.max(distribution, function (d) {
            return d.count;
        });
        var yScale =
            d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

        var xAxis = d3.axisBottom(xScale)
        //.ticks(6,'s');
            .tickValues(chartData.bins);

        var yAxis = d3.axisLeft(yScale);
        yAxis.ticks(5);

        var barHolder = svg.append('g').classed('bar-holder', true);

        var tooltip =
            d3.select('#' + tempString).append("div").attr("class", "toolTip");

        // draw the bars
        var bars =
            barHolder.selectAll('rect.bar')
                .data(distribution)
                .enter()
                .append('rect')
                .classed('bar', true)
                .attr('x',
                    function (d, i) {
                        // the x value is determined using the
                        // month of the datum
                        return 1 + width / (chartData.counts.length) * i;
                    })
                .attr('width', width / (chartData.counts.length))
                .attr('y', function (d) {
                    return yScale(d.count);
                })
                .attr('fill', '#0099ff')
                .attr('height',
                    function (d) {
                        // the bar's height should align it with the base of the
                        // chart (y=0)
                        return height - yScale(d.count);
                    })
                .on("mouseover",
                    function (d) {
                        tooltip.style("left", d3.event.pageX - 20 + "px")
                            .style("top", d3.event.pageY - 30 + "px")
                            .style("display", "inline-block")
                            .html((d.count));
                    })
                .on("mouseout", function (d) {
                    tooltip.style("display", "none");
                });

        // add the average line
        svg.append("line")
            .attr("x1", xScale(avg)) //<<== change your code here
            .attr("y1", 0)
            .attr("x2", xScale(avg)) //<<== and here
            .attr("y2", height)
            .style("stroke", '#000000')
            .style("stroke-width", 2);

        // add \mu text
        svg.append("text")
            .attr("x", xScale(avg) - 5)
            .attr("y", margin.top / 4)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text("\u03bc");

        // add the stdev line
        svg.append("line")
            .attr("x1", xScale(avg)) //<<== change your code here
            .attr("y1", margin.top / 2)
            .attr("x2", xScale(avg + stdev)) //<<== and here
            .attr("y2", margin.top / 2)
            .style("stroke", '#000000')
            .style("stroke-width", 2);

        // add \sigma text
        svg.append("text")
            .attr("x", (xScale(avg) + xScale(avg + stdev)) / 2)
            .attr("y", margin.top / 4)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text("\u03c3");
        // add the stdev rectangle
        svg.append("rect")
            .attr("x", xScale(avg)) //<<== change your code here
            .attr("y", margin.top / 2)
            .attr("width", xScale(avg + stdev) - xScale(avg)) //<<== and here
            .attr("height", height - margin.top / 2)
            .attr('fill', '#0099ff')
            .style("stroke", "red")
            .style("stroke-dasharray", ("3, 3"))
            .style("fill-opacity", 0.2);

        // draw the axes
        svg.append('g')
            .classed('x axis', true)
            .attr('transform', 'translate(0,' + height + ')')
            .call(xAxis);

        var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

        // add a label to the yAxis
        svg.append('text')
            .attr('transform', 'rotate(-90)')
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .style('text-anchor', 'middle')
            .style('fill', 'black')
            .attr('dy', '1em')
            .style('font-size', 10)
            .text('Count');

    } else if (navAggRawData[col - colOffset][row].chartType == 3) {
        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";

        let chartData = navAggRawData[col - colOffset][row]['chartData'];
        let distribution = [];

        for (let i = 0; i < chartData.counts.length; i++) {
            let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
            distribution.push({boundary: boundstr, count: chartData.counts[i]});
        }
        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }

        var margin = {top: 20, right: 25, bottom: 18, left: 35};
        // here, we want the full chart to be 700x200, so we determine
        // the width and height by subtracting the margins from those values
        var fullWidth = wrapperWidth * 0.14;
        var fullHeight = nav.getRowHeight(row);

        // the width and height values will be used in the ranges of our scales
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform',
                'translate(' + margin.left + ',' + margin.top + ')');

        svg.append("rect")
            .attr("x", width + margin.right / 4)
            .attr("y", 0 - margin.top)
            .attr("width", margin.right)
            .attr("height", fullHeight)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", 0 - (margin.top / 2))
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(value);

        let xScale =
            d3.scaleLinear()
                .domain([
                    chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                ])
                .range([0, width]);

        // y value determined by temp
        var maxValue = d3.max(distribution, function (d) {
            return d.count;
        });
        var yScale =
            d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

        var xAxis = d3.axisBottom(xScale)
        //.ticks(6,'s');
            .tickValues(chartData.bins);

        var yAxis = d3.axisLeft(yScale);
        yAxis.ticks(5);

        var barHolder = svg.append('g').classed('bar-holder', true);

        var tooltip =
            d3.select('#' + tempString).append("div").attr("class", "toolTip");

        // draw the bars
        var bars =
            barHolder.selectAll('rect.bar')
                .data(distribution)
                .enter()
                .append('rect')
                .classed('bar', true)
                .attr('x',
                    function (d, i) {
                        // the x value is determined using the
                        // month of the datum
                        return 1 + width / (chartData.counts.length) * i;
                    })
                .attr('width', width / (chartData.counts.length))
                .attr('y', function (d) {
                    return yScale(d.count);
                })
                .attr('fill', '#0099ff')
                .attr('height',
                    function (d) {
                        // the bar's height should align it with the base of the
                        // chart (y=0)
                        return height - yScale(d.count);
                    })
                .on("mouseover",
                    function (d) {
                        tooltip.style("left", d3.event.pageX - 20 + "px")
                            .style("top", d3.event.pageY - 30 + "px")
                            .style("display", "inline-block")
                            .html((d.count));
                    })
                .on("mouseout", function (d) {
                    tooltip.style("display", "none");
                });

        // draw the axes
        svg.append('g')
            .classed('x axis', true)
            .attr('transform', 'translate(0,' + height + ')')
            .call(xAxis);

        var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

        // add a label to the yAxis
        svg.append('text')
            .attr('transform', 'rotate(-90)')
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .style('text-anchor', 'middle')
            .style('fill', 'black')
            .attr('dy', '1em')
            .style('font-size', 10)
            .text('Count');

    } else if (navAggRawData[col - colOffset][row].chartType == 4) {

        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";

        let data = navAggRawData[col - colOffset][row];
        let chartData = data['chartData'];
        let distribution = [];

        for (let i = 0; i < chartData.counts.length; i++) {
            let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
            distribution.push({boundary: boundstr, count: chartData.counts[i]});
        }
        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }

        var margin = {top: 20, right: 25, bottom: 18, left: 35};
        // here, we want the full chart to be 700x200, so we determine
        // the width and height by subtracting the margins from those values
        var fullWidth = wrapperWidth * 0.14;
        var fullHeight = nav.getRowHeight(row);

        // the width and height values will be used in the ranges of our scales
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform',
                'translate(' + margin.left + ',' + margin.top + ')');

        svg.append("rect")
            .attr("x", width + margin.right / 4)
            .attr("y", 0 - margin.top)
            .attr("width", margin.right)
            .attr("height", fullHeight)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", 0 - (margin.top / 2))
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("font-weight", "bold")
            .text(function () {
                if(navAggRawData[col - colOffset][row].formula.includes("COUNTIF"))
                {
                    let percent = (value*100.0)/cumulativeData[currLevel][row].value;
                    return value+ " ("+percent.toFixed(2)+"%)";
                }

                return value;
            });

        let xScale =
            d3.scaleLinear()
                .domain([
                    chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                ])
                .range([0, width]);

        // y value determined by temp
        var maxValue = d3.max(distribution, function (d) {
            return d.count;
        });
        var yScale =
            d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

        var xAxis = d3.axisBottom(xScale)
        //.ticks(6,'s');
            .tickValues(chartData.bins);

        var yAxis = d3.axisLeft(yScale);
        yAxis.ticks(5);

        var barHolder = svg.append('g').classed('bar-holder', true);

        var tooltip =
            d3.select('#' + tempString).append("div").attr("class", "toolTip");

        // draw the bars
        var bars =
            barHolder.selectAll('rect.bar')
                .data(distribution)
                .enter()
                .append('rect')
                .classed('bar', true)
                .attr('x',
                    function (d, i) {
                        // the x value is determined using the
                        // month of the datum
                        return 1 + width / (chartData.counts.length) * i;
                    })
                .attr('width', width / (chartData.counts.length))
                .attr('y', function (d) {
                    return yScale(d.count);
                })
                .attr('fill', '#0099ff')
                .attr('height',
                    function (d) {
                        // the bar's height should align it with the base of the
                        // chart (y=0)
                        return height - yScale(d.count);
                    })
                .on("mouseover",
                    function (d) {
                        tooltip.style("left", d3.event.pageX - 20 + "px")
                            .style("top", d3.event.pageY - 30 + "px")
                            .style("display", "inline-block")
                            .html((d.count));
                    })
                .on("mouseout", function (d) {
                    tooltip.style("display", "none");
                });

        // draw the axes
        svg.append('g')
            .classed('x axis', true)
            .attr('transform', 'translate(0,' + height + ')')
            .call(xAxis);

        var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

        // add a label to the yAxis
        svg.append('text')
            .attr('transform', 'rotate(-90)')
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .style('text-anchor', 'middle')
            .style('fill', 'black')
            .attr('dy', '1em')
            .style('font-size', 10)
            .text('Count');

        let pivotValue = data.pivotValue;
        svg.append("line")
            .attr("x1", xScale(pivotValue)) //<<== change your code here
            .attr("y1", 0 - margin.top / 4)
            .attr("x2", xScale(pivotValue)) //<<== and here
            .attr("y2", height + margin.top / 4)
            .style("stroke", '#000000')
            .style("stroke-dasharray", ("3, 3"))
            .style("stroke-width", 2);

        let dir = data.expandDirection;
        if (dir != 0) {
            // add the rectangle
            svg.append("rect")
                .attr("x",
                    function () {
                        if (dir == 1)
                            return 0;
                        return xScale(pivotValue);
                    }) //<<== change your code here
                .attr("y", yScale(maxValue))
                .attr("width",
                    function () {
                        if (dir == 1)
                            return xScale(pivotValue) - xScale(chartData.bins[0]);
                        return xScale(chartData.bins[chartData.bins.length - 1]) -
                            xScale(pivotValue);
                    }) //<<== and here
                .attr("height", height - yScale(maxValue))
                .attr('fill', '#ffffff')
                .style("stroke", "black")
                //.style("stroke-dasharray", ("3, 3"))
                .style("fill-opacity", 0.7);
        }

    } else {
        // Handsontable.renderers.TextRenderer.apply(this, arguments);
        // td.className = "htCenter htMiddle";

        let tempString = "chartdiv" + row + col;
        td.innerHTML = "<div id=" + tempString + " ></div>";
        let data = navAggRawData[col - colOffset][row];
        let min = navAggRawData[col - colOffset][0]['value'];
        let max = navAggRawData[col - colOffset][0]['value'];
        for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
            if (navAggRawData[col - colOffset][i]['value'] < min) {
                min = navAggRawData[col - colOffset][i]['value'];
            } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                max = navAggRawData[col - colOffset][i]['value'];
            }
        }
        var margin = {top: 20, right: 30, bottom: 0, left: -20};
        var fullHeight = (wrapperHeight * 0.95 / cumulativeData[currLevel].length > 90)
            ? wrapperHeight * 0.95 / cumulativeData[currLevel].length - 10 : 80;
        if(childHash.has(row)){
            let result = childHash.get(row);
            let number = result.length;
            fullHeight += 10;
            if (number > 6) {
                fullHeight += (number - 6) * 5;
            }
        }
        var fullWidth = wrapperWidth * 0.14;
        //console.log("row: " + row + " " + fullHeight+" "+ wrapperHeight+" "+ fullWidth+" "+wrapperWidth);

        // the width and height values will be used in the ranges of our scales
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select('#' + tempString)
            .append('svg')
            .attr('width', fullWidth)
            .attr('height', fullHeight)
            // this g is where the bar chart will be drawn
            .append('g')
            // translate it to leave room for the left and top margins
            .attr('transform',
                'translate(' + margin.left + ',' + margin.top + ')');
        /*svg.append("rect")
            .attr("x", width)
            .attr("y", 0 - margin.top)
            .attr("width", margin.right)
            .attr("height", fullHeight + 10)
            .attr("fill", d3.interpolateGreens(
                ((value - min) / (max - min)) * 0.85 + 0.15));*/
        svg.append("rect")
            .attr("x", 0)
            .attr("y", 0-margin.top)
            .attr("width", fullWidth*(value)/max)
            .attr("height", fullHeight)
            .attr("fill", '#B2EEB4');

        svg.append("text")
            .attr("x", (width / 2))
            .attr("y", height/2 + margin.top/2)
            .attr("text-anchor", "middle")
            .style("font-size", "20px")
            .style("font-weight", "bold")
            .text(function () {
                if(navAggRawData[col - colOffset][row].formula.includes("COUNTIF"))
                {
                    let percent = (value*100.0)/cumulativeData[currLevel][row].value;
                    return value+ " ("+percent.toFixed(2)+"%)";
                }
                return value;
            });

    }

    td.style.background = '#FAF2ED';
    return td;
}

var colors = ['#c799cc','#eba6ee', '#ea7beb', '#fa1aec']

function updataHighlight(child) {
    let brushNLinkRows = [];
    if(navAggRawData.length == 1 && isPointFormula(navAggRawData[0][0].formula)) {
        let data = navAggRawData[0];
        let queryObj = {}
        let cond = [];
        let value = [];
        let firstR = [];
        let lastR = [];

        for (let i = 0; i < selectedChild.length; i++) {

            let formula = data[selectedChild[i]].formula;

            if (formula.includes("COUNTIF") || formula.includes("SUMIF")) {
                let ls = formula.split(",")[1].split(")")[0];
                let str = ls.substring(1, 3);
                if(str.includes(">=") || str.includes("<=") || str.includes("<>"))
                {
                    cond.push(ls.substring(1, 3));
                    value.push(ls.substring(3, ls.length - 1));
                }
                else if(str.includes(">") || str.includes("<") || str.includes("="))
                {
                    cond.push(ls.substring(1, 2));
                    value.push(ls.substring(2, ls.length - 1));
                }
                else
                    value.push(ls.substring(1, ls.length - 1));

            }
            else if (formula.includes("MIN") || formula.includes("MAX") || formula.includes("MEDIAN") || formula.includes("MODE") || formula.includes("RANK") || formula.includes("SMALL") || formula.includes("LARGE")) {
                value.push(data[selectedChild[i]].value);
            }

            //TODO: when ondemand loading of data available
            /*let first = cumulativeData[currLevel][selectedChild[i]].rowRange[0];
            let last = cumulativeData[currLevel][selectedChild[i]].rowRange[1];

            if (first < currentFirstRow)
                firstR.push(currentFirstRow)
            else
                firstR.push(first);
            if (last > currentLastRow)
                lastR.push(currentLastRow);
            else
                lastR.push(last);*/
            if(lowerRange==0)
                firstR.push(lowerRange+1);
            else
                firstR.push(lowerRange);
            lastR.push(upperRange);
        }

        queryObj.bookId = bId;
        queryObj.sheetName = sName;
        queryObj.index = hierarchicalColAttr[0];
        queryObj.first = firstR;
        queryObj.last = lastR;
        queryObj.conditions = cond;
        queryObj.values = value;

        $.ajax({
            url: baseUrl + "getBrushColorList",
            method: "POST",
            // dataType: 'json',
            contentType: 'text/plain',
            data: JSON.stringify(queryObj),
        }).done(function (e) {
            if (e.status == "success") {
                console.log(e.data);//#d4eafc

                brushNLinkRows = e.data;

            }



        });

    }
    hot.updateSettings({
        cells: function (row, column, prop) {
            let cellMeta = {}
            if (column == exploreAttr - 1) {
                cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                              cellProperties) {
                    Handsontable.renderers.TextRenderer.apply(this, arguments);
                    //td.style.background = '#70DCB8';
                    td.style.background = '#32CC99';
                }
            }

            if(hierarchicalColAttr.includes(column))
            {
                cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                              cellProperties) {
                    Handsontable.renderers.TextRenderer.apply(this, arguments);
                    if(brushNLinkRows.length!=0)
                    {
                        if(brushNLinkRows.includes(row))
                            td.style.background = '#d4eafc';
                        else
                            td.style.background = '#f5e9e1';

                    }
                    else
                        td.style.background = '#f5e9e1';
                }
            }

            if (child != undefined) {
                let lower = cumulativeData[currLevel][child].rowRange[0];
                let upper = cumulativeData[currLevel][child].rowRange[1];
                for (let i = 0; i < sortAttrIndices.length; i++) {
                    if (column == (sortAttrIndices[i] - 1) && row >= lower &&
                        row <= upper) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop,
                                                      value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = colors[i];
                        }
                    }
                }
            }

            // if(brushNLinkRows.length!=0)
            // {
            //     cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
            //                                   cellProperties) {
            //         Handsontable.renderers.TextRenderer.apply(this, arguments);
            //         if(brushNLinkRows.includes(row) && column < 16 && column != exploreAttr - 1)
            //             td.style.background = '#d4eafc';
            //     }
            // }
            return cellMeta;
        }
    });
}


function updateBarChartFocus(firstRow, lastRow)
{
    //console.log(childHash);
    let newSelectedBars = [];
    for(let selI=0;selI<cumulativeData[currLevel].length;selI++) {
        if (childHash.get(selI) == undefined)
            continue;

        let newSelectedBar = [];
        //console.log(childHash.get(selI));
        for (let selJ = 0; selJ < childHash.get(selI).length; selJ++) {
            let lower = childHash.get(selI)[selJ].rowRange[0];
            let upper = childHash.get(selI)[selJ].rowRange[1];

            //console.log("lowerRange,upperRange", lower, upper);
            if (firstRow > upper)
                continue;
            if (lastRow < lower)
                break;
            newSelectedBar.push(selJ);
        }

        if (newSelectedBar.length > 0) {
            let barObj = {};
            barObj.cell = selI;
            barObj.bars = newSelectedBar;
            newSelectedBars.push(barObj);
        }
    }

    //console.log("newselectedBars");
    //console.log(newSelectedBars);
    if(newSelectedBars.length==1 && selectedBars.length==1 && newSelectedBars[0].cell == selectedBars[0].cell && newSelectedBars[0].bars.length == selectedBars[0].bars.length)
    {
        for(let selI=0; selI < newSelectedBars[0].bars.length;selI++)
        {
            if(newSelectedBars[0].bars[selI]!=selectedBars[0].bars[selI]) {
                selectedBars = [];
                selectedBars = newSelectedBars;
                return false;
            }
        }
        return true;
    }

    selectedBars = [];
    selectedBars = newSelectedBars;
    return false;

}

function updateNavCellFocus(firstRow, lastRow)
{

    console.log("firstRow,lastRow:",firstRow,lastRow);
    //console.log(cumulativeData[currLevel]);

    //console.log("lowerRange,upperRange",lowerRange,upperRange);
    currentFirstRow = firstRow;
    currentLastRow = lastRow;

    let newSelectedChild = [];
    for(let selI=0;selI<cumulativeData[currLevel].length;selI++)
    {
        let lower = cumulativeData[currLevel][selI].rowRange[0];
        let upper = cumulativeData[currLevel][selI].rowRange[1];

        //console.log("lowerRange,upperRange",lower,upper);
        if(firstRow > upper)
            continue;
        if(lastRow < lower)
            break;
        newSelectedChild.push(selI);
    }

    if(newSelectedChild.length==1)
    {
        if(selectedChild.length==0)
        {
            nav.deselectCell();
            selectedChild = [];
            selectedChild = newSelectedChild;
            updateBarChartFocus(firstRow, lastRow);
            nav.render();
        }
        else if(selectedChild.length> 1)
        {
            nav.deselectCell();
            selectedChild = [];
            selectedChild = newSelectedChild;
            updateBarChartFocus(firstRow, lastRow);
            nav.render();
        }
        else if(selectedChild[0]!=newSelectedChild[0])
        {
            nav.deselectCell();
            selectedChild = [];
            selectedChild = newSelectedChild;
            updateBarChartFocus(firstRow, lastRow);
            nav.render();
        }
        else
        {
            updateBarChartFocus(firstRow, lastRow);
            nav.render();
        }

    }
    else if(newSelectedChild.length > 1)
    {
        nav.deselectCell();
        selectedChild = [];
        selectedChild = newSelectedChild;
        updateBarChartFocus(firstRow, lastRow);
        nav.render();
    }
}

function brushNlink(firstRow, lastRow) {
    console.log("brush and link");

    let path = computePath();

   // console.log("path: "+path);


    let currentFocus = cumulativeData[currLevel];
    if(currentFocus==undefined)
        return;

    //console.log(currentFocus);
    let lastElement = currentFocus[currentFocus.length-1];
    let firstElement = currentFocus[0];
   // console.log(lastElement)
    let endRow = lastElement.rowRange[1];
    let startRow = firstElement.rowRange[0];

    console.log("endRow: "+endRow+", firstRow: "+firstRow);
    console.log("startRow: "+startRow+", lastRow: "+lastRow);

    if(startRow > lastRow)
    {
        jumpToFocus(prevPath,nav);
        updateNavCellFocus(firstRow, lastRow);
    }
    else if(endRow < firstRow)
    {
        jumpToFocus(nextPath,nav);
        updateNavCellFocus(firstRow, lastRow);
    }
    else
    {
        updateNavCellFocus(firstRow, lastRow);
    }

    //updateSScolor(firstRow,lastRow);

}

function isPointFormula(formula) {
    let str = formula.split("(")[0];
    if(pointFunc.includes(str))
        return true;
    return false;
}

function jumpToFocus(path, nav) {
    nav.deselectCell();

    childHash = new Map();
    console.log("nextPath:"+path);
    let path_str = "";
    levelList = []
    for (let i = 0; i < path.length; i++) {
        if (path.length != 0) {
            levelList[i] = parseInt(path[i]);
            if(i==0)
                path_str += path[i];
            else if(i<path.length-1)
                path_str += ","+path[i];
        }
    }
    selectedChild = [];
    selectedBars = [];

    let queryData = {};


    queryData.bookId = bId;
    queryData.sheetName = sName;
    queryData.path = path_str;

    console.log("queryData:");
    console.log(queryData);
    $.ajax({
        url: baseUrl + "getChildren",
        method: "POST",
        //dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(queryData),
    }).done(function (e) {
        if (e.status == "success") {
            var result = e.data;
            console.log(result);
            currData = result.buckets;

            prevPath = result.prev.path;
            nextPath = result.later.path;
            let breadcrumb_ls = result.breadCrumb;
            currLevel = breadcrumb_ls.length;
            let numChild = currData.length;
            viewData = new Array(numChild);

            if (currLevel == 0) {
                colHeader.splice(1, 0, "")
            }
            console.log(result);
            console.log("currLevel: " + currLevel);
            mergeCellInfo = [];
            if (currData.length != 0 && breadcrumb_ls.length!=0) {
                for(let i=0;i<currData.length;i++)
                {
                    childHash.set(i,currData[i].children);
                }
                mergeCellInfo.push({row: 0, col: 0, rowspan: currData.length, colspan: 1});

                viewData = new Array(currData.length);
                for (let i = 0; i < currData.length; i++) {
                    if (i == 0) {
                        viewData[i] = [breadcrumb_ls[breadcrumb_ls.length-1]];
                    } else {
                        viewData[i] = [""];
                    }
                }

                cumulativeData.pop();

                cumulativeData.push(currData);

                for (let i = 0; i < currData.length; i++) {
                    //double layer
                    viewData[i][1] = cumulativeData[currLevel][i].name;

                }

                //console.log(viewData);

                cumulativeDataSize += currData.length;
            }
            else if (currData.length != 0 && breadcrumb_ls.length==0)
            {
                for(let i=0;i<currData.length;i++)
                {
                    childHash.set(i,currData[i].children);
                }
                //colHeader.splice(1, 1);
                cumulativeData = [];
                cumulativeData.push(currData);
                for (let i = 0; i < numChild; i++) {
                    viewData[i] = [currData[i].name];
                }
                cumulativeDataSize += currData.length;
            }
            else {

                path.splice(-1,1);
                jumpToFocus(path,nav);
                return;
            }


            let columWidth = [];
            if (currLevel >= 1) {
                //columWidth = [50];
            } else {
                columWidth = 200;
            }

            if (hieraOpen) {
                getAggregateValue();

            } else {
                nav.updateSettings({
                    // minRows: currData.length,
                    data: viewData,
                    rowHeights: (wrapperHeight * 0.95 / currData.length > 80) ? wrapperHeight * 0.95 / currData.length : 80,
                    mergeCells: mergeCellInfo,
                });
                zoomming = false;
                //nav.selectCell(0, 1)
            }
            updateNavPath(breadcrumb_ls); //calculate breadcrumb
            updateNavCellFocus(currentLastRow,currentLastRow);
            // zoomming = false;
            //  nav.selectCell(0, 1)
            nav.render();
        }
    })
}
