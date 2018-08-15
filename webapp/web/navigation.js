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

var navHistroyTable = {};
var navHistoryPathIndex = [];
var breadCrumbHistoryPathIndex = [];

var options = [];
var hieraOpen = false;
var exploreOpen = false;
var attr_index = [];
var funcId = [];

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

var currData;
var zoomming = false;
var zoomouting = false;
var targetChild;

var sortOptionString = "";
var sortTotalNum = 0;

var aggregateTotalNum = 0;
var aggregateColStr = "";
var aggregateOptStr = "";
var aggregateData = {};
var navRawFormula;
var navAggRawData = [];

var exploreAttr;

var sortAttrIndices = [];

var selectedChild = 0;
var lowerRange;
var upperRange;

var currRange;

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
            $(this).nextAll().remove();
            switch (this.value) {
                case "COUNTIF":
                case "SUMIF":
                    $(this).after(
                        "<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
                    break;
                case "LARGE":
                case "SMALL":
                    $(this).after(
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
                    $(this).after(tempString);
                    break;
                case "RANK":
                    tempString =
                        "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>";
                    tempString +=
                        "<select class='' id='aggrePara00'><option value='1' selected >ascending</option><option value='0'>descending</option></select>"
                    $(this).after(tempString);
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
            width: $('.wrapper').width() * 0.59,
        });
    } else {
        hot.updateSettings({
            width: $('.wrapper').width() * 0.79,
        });
    }
    $("#hierarchical-col").css("display", "none");
    $("#test-hot").css({"float": "left"});
    $("#exploration-bar").css({
        "display": "inline",
        "float": "left",
        "width": "19%",
        "height": wrapperHeight * 0.95
    });
})

// hierarchical formula builder: for each line
function createAggreString() {
    let tempString = "<div><select class='custom-select my-1' id='aggregateCol" +
        aggregateTotalNum +
        "''><option value='' disabled selected hidden>Attribute" +
        aggregateTotalNum + "</option>";
    if (aggregateTotalNum == 0) {
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
        aggregateTotalNum +
        "''><option value='' disabled selected hidden>Function" +
        aggregateTotalNum + "</option>";
    if (aggregateTotalNum == 0) {
        for (let i = 0; i < funcOptions.length; i++) {
            aggregateOptStr += "<option value='" + funcOptions[i] + "''>" +
                funcOptions[i] + "</option>";
        }
        aggregateOptStr += "</select></div>";
        tempString += aggregateOptStr;
    } else {
        tempString += aggregateOptStr;
    }
    aggregateTotalNum += 1;
    return tempString;
}

// for adding more aggregate attribute
$("#aggreAdd").click(function () {
    var $aggregateCol = $("#aggregateCol");
    $aggregateCol.append(createAggreString());
    $("#aggregateOpt" + (aggregateTotalNum - 1)).change(function (e) {
        let number = e.target.id.charAt(e.target.id.length - 1)

        // Do something with the previous value after the change
        $(this).nextAll().remove();
        switch (this.value) {
            case "COUNTIF":
            case "SUMIF":
                $(this).after(
                    "<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                    number + "'>");
                break;
            case "LARGE":
            case "SMALL":
                $(this).after(
                    "<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                    number + "'>");
                break;
            case "SUBTOTAL":
                let tempString =
                    "<select class='' id='aggrePara" + number +
                    "'><option value='' disabled selected hidden>Function_num</option>";
                for (let i = 0; i < subtotalFunc.length; i++) {
                    tempString +=
                        "<option value='" + (i + 1) + "''>" + subtotalFunc[i] + "</option>";
                }
                tempString += "</select>";
                $(this).after(tempString);
                break;
            case "RANK":
                tempString =
                    "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara" +
                    number + "'>";
                tempString +=
                    "<select class='' id='aggrePara" + number + number +
                    "'><option value='0' selected >ascending</option><option value='1'>descending</option></select>"
                $(this).after(tempString);
                break;
        }
    });
})

$("#aggreRemove").click(function () {
    if (aggregateTotalNum > 1) {
        $("#aggregateCol").children().last().remove();
        aggregateTotalNum -= 1;
    }
})

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
        sortOptionString += "</select>";
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
            width: $('.wrapper').width() * 0.79,
        });
    } else {
        hot.updateSettings({
            width: $('.wrapper').width() * 0.99,
        });
    }
})

// navigation start, showing left column.
function Explore(e) {
    $("#navPath")
        .css({"display": "block", "height": "5%"}); // add the breadcrumb
    $("#Hierarchical").click(function () { // handling hierarchical column click in
        // the Exploration Tools
        $("#exploration-bar").css("display", "none");
        hot.updateSettings({width: wrapperWidth * 0.59});
        $("#hierarchical-col").css({
            "float": "left",
            "width": "19%",
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
    })

    $("#test-hot").css({"float": "left"});
    $("#navChart").css({"display": "inline", "float": "left"});

    $("#history-option").empty();

    $.get(baseUrl + 'startNav/' + bId + '/' + sName + '/' + e, function (data) {
        clickable = true;
        currLevel = 0;
        levelList = [];
        spanList = [];
        cumulativeData = [];

        mergeCellInfo = [];
        colHeader = [options[e - 1]];
        cumulativeDataSize = 0;

        currData = data.data;
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

        cumulativeDataSize += currData.length;

        hot.updateSettings({
            width: wrapperWidth * 0.8,
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
            rowHeights: (wrapperHeight * 0.95 / currData.length > 80)
                ? wrapperHeight * 0.95 / currData.length
                : 80,
            // startRows: 200,
            //  startCols: 5,
            width: wrapperWidth * 0.19,
            height: wrapperHeight * 0.95,
            rowHeaderWidth: 0,
            rowHeaders: true,
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
            // colHeaders: colHeader,
            // fixedRowsTop: 11,
            // fixedColumnsLeft: 1,
            stretchH: 'all',
            contextMenu: false,
            outsideClickDeselects: false,
            // manualColumnResize: true,
            // manualRowResize: true,
            className: "htCenter htMiddle wrap",

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

                if (topLevel || otherLevel || zoomming ||
                    e.realTarget.className == "colHeader" ||
                    e.realTarget.className == "relative") {
                    e.stopImmediatePropagation();
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
                    selectedChild = r;
                    lowerRange = cumulativeData[currLevel][r].rowRange[0];
                    upperRange = cumulativeData[currLevel][r].rowRange[1];
                    updateData(cumulativeData[currLevel][r].rowRange[0], 0,
                        cumulativeData[currLevel][r].rowRange[1], 15, true);
                    nav.render();
                }
            },

            data: viewData,
            cells: function (row, column, prop) {
                let cellMeta = {}
                if (currLevel == 0) {
                    if (column == 0 && row == selectedChild) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                                      cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#D3D3D3';
                            td.style.color = 'white';
                        }
                    } else if (column == 0) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                                      cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#F5F5DC';
                        }
                    } else {
                        cellMeta.renderer = chartRenderer;
                    }
                }
                else {
                    if (column == 1 && row == selectedChild) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                                      cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#D3D3D3';
                            td.style.color = 'white';
                        }
                    } else if (column <= 1) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                                      cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#F5F5DC';
                        }
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
        nav.selectCell(0, 0);

        updateData(0, 0, 1000, 15, true);
        lowerRange = 0;
        upperRange = 1000;
        updataHighlight();

        //   doubleclick implementation option2:
        nav.view.wt.update('onCellDblClick', function (e, cell) {
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

function removeHierarchiCol(colIdx) {
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
    }
    nav.alter('remove_col', colIdx);

    // nav.render();
}

$("#hierarchi-form").submit(function (e) {
    aggregateData.bookId = bId;
    aggregateData.sheetName = sName;
    e.preventDefault();
    aggregateData.formula_ls = [];
    // attr_index = []
    // funcId = []
    hieraOpen = false;
    let getChart = ($("#chartOpt").val() == 2);
    for (let i = 0; i < aggregateTotalNum; i++) {
        let attrIdx = $("#aggregateCol" + i).val();
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
                para = $("#aggrePara" + i).val();
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
                para = $("#aggrePara" + i).val();
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
                para = $("#aggrePara" + i).val();
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
                para = $("#aggrePara" + i).val();
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
    getAggregateValue();
});

function getAggregateValue() {
    let childlist = computePath();
    aggregateData.path = " " + childlist;

    $.ajax({
        url: baseUrl + "getHierarchicalAggregateFormula",
        method: "POST",
        // dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(aggregateData),
    }).done(function (e) {
        if (e.status == "success") {
            $("#hierarchical-col").css("display", "none");
            hot.updateSettings({width: wrapperWidth * 0.8});
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
    nav.updateSettings({
        manualColumnResize: columWidth,
        minCols: 1,
        data: viewData,
        rowHeights: (wrapperHeight * 0.95 / numChild > 80)
            ? wrapperHeight * 0.95 / numChild
            : 80,

        // maxCols: 3,
        //  fixedColumnsLeft: targetCol,
        mergeCells: mergeCellInfo,
    });
    if (zoomming) {
        zoomming = false;
        nav.selectCell(0, 1);
    }
    if (zoomouting) {
        zoomouting = false;
        if (currLevel == 0) {
            nav.selectCell(targetChild, 0)
        } else {
            nav.selectCell(targetChild, 1);
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
            currLevel += 1;
            currData = result.buckets;
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
                    rowHeights: (wrapperHeight * 0.95 / currData.length > 80)
                        ? wrapperHeight * 0.95 / currData.length
                        : 80,
                    mergeCells: mergeCellInfo,
                });
                zoomming = false;
                nav.selectCell(0, 1)
            }
            updateNavPath(breadcrum_ls); // calculate breadcrumb
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
            rowHeights: (wrapperHeight * 0.95 / numChild > 80)
                ? wrapperHeight * 0.95 / numChild
                : 80,
            mergeCells: mergeCellInfo,
        });
        zoomouting = false;
        if (currLevel == 0) {
            nav.selectCell(targetChild, 0)
        } else {
            nav.selectCell(targetChild, 1);
        }
    }

    updateNavPath();

    // nav.render();
}

function zoomOutHist(nav) {

    clickable = true;
    nav.deselectCell();
    targetChild = levelList[levelList.length - 1];

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
            var result = e.data;
            let breadcrum_ls = result.breadCrumb;
            // clickable = result.clickable;
            currLevel -= 1;
            currData = result.buckets;
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
                    rowHeights: (wrapperHeight * 0.95 / numChild > 80)
                        ? wrapperHeight * 0.95 / numChild
                        : 80,
                    mergeCells: mergeCellInfo,
                });
                zoomouting = false;
                if (currLevel == 0) {
                    nav.selectCell(targetChild, 0)
                } else {
                    nav.selectCell(targetChild, 1);
                }
            }

            updateNavPath(breadcrum_ls);
        }
    })
}

function jumpToHistorialView(childlist) {

    clickable = true;
    nav.deselectCell();

    let tmp_ls = childlist.split(",");
    levelList = []
    for (let i = 0; i < tmp_ls.length; i++) {
        if (tmp_ls[i].length != 0)
            levelList[i] = parseInt(tmp_ls[i]);
    }
    targetChild = levelList[levelList.length - 1];
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
                    rowHeights: (wrapperHeight * 0.95 / numChild > 80)
                        ? wrapperHeight * 0.95 / numChild
                        : 80,
                    mergeCells: mergeCellInfo,
                });
                zoomouting = false;
                if (breadcrumb_ls.length == 0) {
                    nav.selectCell(targetChild, 0)
                } else {
                    nav.selectCell(targetChild, 1);
                }
            }

            updateNavPath(breadcrumb_ls);
        }
    });
}

$("#sort-form").submit(function (e) {
    e.preventDefault();
    $("#exampleModal").modal('hide')
    sortAttrIndices = [];
    for (let i = 0; i < sortTotalNum; i++) {
        sortAttrIndices.push($('#inlineOpt' + i).val());
    }
    var selectedArray = nav.getSelected();
    //  var child = selectedArray[0][0]/spanList[currLevel];
    var child = selectedArray[0][0]
    let childlist = computePath();
    let path = ""
    if (childlist !== "") {
        path += childlist + ',' + child
    }
    else {
        path = child
    }

    $.get(baseUrl + 'sortBlock/' + bId + '/' + sName + '/ ' + path + '/' +
        sortAttrIndices + '/' + 0,
        function (data) {
            updateData(cumulativeData[currLevel][child].rowRange[0], 0,
                cumulativeData[currLevel][child].rowRange[1] + 10, 15,
                true)

            updataHighlight(child);
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
            } else if (navAggRawData[col - colOffset][i]['value'] > min) {
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
            } else if (navAggRawData[col - colOffset][i]['value'] > min) {
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
                            return '#ffa158'
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
            } else if (navAggRawData[col - colOffset][i]['value'] > min) {
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
            } else if (navAggRawData[col - colOffset][i]['value'] > min) {
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
            } else if (navAggRawData[col - colOffset][i]['value'] > min) {
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
        Handsontable.renderers.TextRenderer.apply(this, arguments);
    }

    td.style.background = '#FAEBD7';
    return td;
}

var colors = ['#32CC99', '#70DCB8', '#ADEBD6', '#EBFAF5']

function updataHighlight(child) {
    hot.updateSettings({
        cells: function (row, column, prop) {
            let cellMeta = {}
            if (column == exploreAttr - 1) {
                cellMeta.renderer = function (hotInstance, td, row, col, prop, value,
                                              cellProperties) {
                    Handsontable.renderers.TextRenderer.apply(this, arguments);
                    td.style.background = '#CEC';
                }
            }
            if (child != undefined) {
                let lower = cumulativeData[currLevel][child].rowRange[0];
                let upper = cumulativeData[currLevel][child].rowRange[1];
                for (let i = 0; i < sortTotalNum; i++) {
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
            return cellMeta;
        }
    });
}
