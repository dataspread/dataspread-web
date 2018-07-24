var baseUrl = 'api/';

window.$ = window.jQuery = $;

$.ajaxSetup({
    headers: {
        'auth-token': "guest"
    }
});

// import handlebars;
var container = document.getElementById('test-hot');

// var searchFiled = document.getElementById('search_field');
var data;
var selectedRange;
var bId;
var sName;
var range;
var bName;
var sheetNames = [];
var sheetData;
var currentSheet;


// mock service for getting data
var fetchData = function (n) {
    return Handsontable.helper.createSpreadsheetData(n, 20);
};

var testData = fetchData(1000);

var compute_window = function (e) {
    var rowCount = hot.countRows();
    var rowOffset = hot.rowOffset();
    var visibleRows = hot.countVisibleRows();
    console.log(rowCount)
    console.log(rowOffset);
    console.log(visibleRows);
    console.log(hot.countRenderedRows())
    //  console.log(hot.getLastVisibleRow())
    var lastRow = rowOffset + (visibleRows * 1);
    var lastVisibleRow = rowOffset + visibleRows + (visibleRows / 2);
    var threshold = 15;
    $(".parallax-one").css({
        height: ((lastRow / currRange) * 80) + "%"
    });

    if (lastVisibleRow > upperRange - threshold) {
        updateData(upperRange, 0, upperRange + 1000, 15, false)
        upperRange = upperRange + 1000;
        console.log("in compute window");
    }
    console.log(lowerRange)
    if (rowOffset < lowerRange - threshold) {
        updateData(rowOffset - 200, 0, rowOffset, 15, false)
        lowerRange = lowerRange - 200;
        console.log("in compute window");
    }
    // if (lastVisibleRow > (rowCount - threshold)) {
    //   loadMoreData(rowCount);
    //  }
};

// load data and render
var loadMoreData = function (n) {
    // call data service
    var incoming = fetchData(n);
    var emptyArray = Array(50).fill(null);

    incoming.forEach(function (d) {
        testData.push(d);
    });
    hot.render();
};


var tempdata = [["<div id=\"chartdiv\"></div>"], ["<dl>\n" +
"  <dt>\n" +
"    Browser market share June 2015\n" +
"  </dt>\n" +
"  <dd class=\"percentage percentage-11\"><span class=\"text\">IE 11: 11.33%</span></dd>\n" +
"  <dd class=\"percentage percentage-49\"><span class=\"text\">Chrome: 49.77%</span></dd>\n" +
"  <dd class=\"percentage percentage-16\"><span class=\"text\">Firefox: 16.09%</span></dd>\n" +
"  <dd class=\"percentage percentage-5\"><span class=\"text\">Safari: 5.41%</span></dd>\n" +
"  <dd class=\"percentage percentage-2\"><span class=\"text\">Opera: 1.62%</span></dd>\n" +
"  <dd class=\"percentage percentage-2\"><span class=\"text\">Android 4.4: 2%</span></dd>\n" +
"</dl>",]];

var wrapperHeight = $(".wrapper").height();
var wrapperWidth = $(".wrapper").width();
//default setting
var ssDefaultSettings = {
    minRows: 200,
    minCols: 50,
    // startRows: 200,
    startCols: 40,
    width: 1200,
    height: 800,
    //width: $(".wrapper").width(),
    //height: wrapperHeight,

    rowHeaders: true,
    colHeaders: true,
    //currentRowClassName: 'currentRow',
    //currentColClassName: 'currentColum',
    //fixedRowsTop: 1,
    contextMenu: true,
    outsideClickDeselects: false,
    manualColumnResize: true,
    manualRowResize: true,
    search: true,
    sortIndicator: true,
    customBorders: true,
    // contextMenu:[],
    afterScrollVertically: function (e) {
        compute_window(e);
        console.log("scroll down");
    },
    // data: tempdata,
    // columns: [{renderer: chartRenderer}],
    // afterChange: function (change, source) {
    //     var updatedData = [];
    //     console.log(change)
    //     console.log(source)
    //     if (change !== null) {
    //         change.forEach(function (e) {
    //             if (e[3].charAt(0) == '=') {
    //                 updatedData.push({
    //                     "row": e[0],
    //                     "col": e[1],
    //                     "value": '',
    //                     "formula": e[3].substring(1),
    //                     "type": 'String',
    //                     "format": ''
    //                 });
    //
    //             } else {
    //                 updatedData.push({
    //                     "row": e[0],
    //                     "col": e[1],
    //                     "value": e[3],
    //                     "formula": '',
    //                     "type": '',
    //                     "format": ''
    //                 });
    //             }
    //
    //         })
    //     }
    //
    //     var update = {
    //         'bookId': bId,
    //         'sheetName': sName,
    //         'range': range,
    //         'cells': updatedData
    //     };
    //     if (!(update.cells.length == 0)) {
    //         $.ajax({
    //             url: baseUrl + "putCells",
    //             method: 'PUT',
    //             headers: {
    //                 'auth-token': 'guest'
    //             },
    //             dataType: "json",
    //             contentType: "application/json",
    //             data: JSON.stringify(update)
    //         })
    //     }
    //
    // },
    beforeSetRangeEnd: function (e) {


    }
    // ,
    // afterScrollVertically: function(e){
    //     compute_window(e);
    // }
    // ,data: testData
};

var SFU = false;
//dynamic setting
var ssDynamicSettings = {
    minRows: 200,
    minCols: 50,
    // startRows: 200,
    startCols: 40,
    width: 1200,
    height: 800,
    rowHeaders: true,
    colHeaders: true,
    contextMenu: true,
    outsideClickDeselects: false,
    manualColumnResize: true,
    manualRowResize: true,
    search: true,
    sortIndicator: true,
    customBorders: true,
    // contextMenu:[],
    afterScrollVertically: function (e) {
        compute_window(e);
        console.log("scroll down");
    }
    ,
    afterSelection: function (row, column, row2, column2, preventScrolling, selectionLayerLevel) {
        console.log("afterSelection")
        console.log(row, column)
        $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + row + "/" + column + "/" + row + "/" + column, function (data) {

            console.log(data)
            console.log(data.data.cells[0].formula)
            if (data.data.cells[0].formula != "") {

                $("#formulaBar").val("=" + data.data.cells[0].formula);
            }
            else {
                $("#formulaBar").text = "";
            }
        })

    },
    afterChange: function (change, source) {
        var updatedData = [];
        console.log(change)
        //alert(source);
        if (source == "populateFromArray") {
            return;
        }
        let formulaCellList = [];
        if (!SFU) {
            if (change !== null) {
                change.forEach(function (e) {
                    console.log(e);
                    if (e[3].charAt(0) == '=') {
                        updatedData.push({
                            "row": e[0],
                            "col": e[1],
                            "value": '',
                            "formula": e[3].substring(1),
                            "type": 'String',
                            "format": ''
                        });
                        formulaCellList.push({
                            "row": e[0],
                            "col": e[1],
                            "value": '',
                            "formula": e[3].substring(1),
                            "type": 'String',
                            "format": ''
                        });

                    } else {
                        updatedData.push({
                            "row": e[0],
                            "col": e[1],
                            "value": e[3],
                            "formula": '',
                            "type": '',
                            "format": ''
                        });
                    }

                })
            }

            var update = {
                'bookId': bId,
                'sheetName': sName,
                'range': range,
                'cells': updatedData
            };
            if (!(update.cells.length == 0)) {
                $.ajax({
                    url: baseUrl + "putCells",
                    method: 'PUT',
                    headers: {
                        'auth-token': 'guest'
                    },
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(update)
                }).done(function (result) {
                    if (formulaCellList.length != 0) {
                        let r1 = formulaCellList[0].row;
                        let c1 = formulaCellList[0].col;
                        $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + r1 + "/" + c1 + "/" + r1 + "/" + c1, function (data) {
                            SFU = true;
                            hot.setDataAtCell(r1, c1, data.data.cells[0].value);
                        })
                    }
                })
            }
        } else {
            SFU = false;
        }


    },
    beforeSetRangeEnd: function (e) {


    }
    // ,
    // afterScrollVertically: function(e){
    //     compute_window(e);
    // }
    // ,data: testData
};
// //initializing interface
var hot = new Handsontable(container, ssDefaultSettings);

var clearCanvas = function (dataArray) {
    if (dataArray != undefined) {
        ssDynamicSettings.data = dataArray;
        hot = new Handsontable(container, ssDynamicSettings);
    } else {
        hot = new Handsontable(container, ssDefaultSettings);

    }
}


var getSheets = function () {
    //get all the sheet name and id and generfate button for each of them
    $.get(baseUrl + "getSheets/" + bId, function (data) {
        //add sheet name to tabs
        var sheets = data['data']['sheets'];
        sName = sheets[0].name;

        // $('#sheets').empty();
        sheets.forEach(function (e) {

            console.log(e);

            $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
        });

        $("#tableName").text($('#book-selector').val());
        sheetData = data;
    })
}

var renderSheets = function (sheets) {
    $("#sheets").empty();
    sheets.forEach(function (e) {
        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e['name'] + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item" href="#">Rename</a><a class="dropdown-item" href="#">Duplicate</a><a class="dropdown-item" href="#">Delete</a><a class="dropdown-item" href="#">Clear</a><a class="dropdown-item" href="#">Move Left</a><a class="dropdown-item" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
    });
}

var reorderSheets = function () {

}

var createSheet = function () {
    //create a new sheet and by default ask people to specify the name of the sheet
    $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + $('#createdSheetName').val() + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

    bindSheetListener();

    sheetNames.push($('#createdSheetName').val());
    sName = $('#createdSheetName').val();
    var update = {
        "bookId": bId,
        "sheetName": sName
    }
    //
    $.ajax({
        url: baseUrl + "addSheet",
        method: 'POST',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {

        $.get(baseUrl + "getSheets/" + bId, function (data) {
            //add sheet name to tabs
            // window.data01=data
            sheetData = data;

            openSheet(bId, sName, data['data']['sheets'].length);

        })
        // openSheet(bId, sheet, sName, sheetNames.length);

    })

}

var renameSheet = function (e) {
    //convert button to clickable field
    //
    currentSheet = e.target.parentNode.parentNode.children[0];
    sName = currentSheet.innerText;


    $("#sheet-dialog").empty();
    $('<label></label>').text("Rename the current Sheet:").appendTo($("#sheet-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "newSheetName").appendTo("#sheet-dialog");
    var dialog = $("#sheet-dialog").dialog({
        buttons: {
            "Change Name": function () {

                var update = {
                    "bookId": bId,
                    "oldSheetName": sName,
                    "newSheetName": $("#newSheetName").val()
                }


                $.ajax({
                    url: baseUrl + "changeSheetName",
                    method: 'PUT',
                    headers: {
                        'auth-token': 'guest'
                    },
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(update)
                }).done(function () {

                    sName = $("#newSheetName").val();


                    // currentSheet.innerText() = sName;
                    currentSheet.innerText = sName
                })

                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    })
}


var deleteSheet = function (e) {
    var currentSheet = e.target.parentNode.parentNode.children[0];
    e.target.parentNode.parentNode.remove();

    var update = {
        "bookId": bId,
        "sheetName": currentSheet.innerText
    }
    $.ajax({
        url: baseUrl + "deleteSheet",
        method: 'DELETE',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    })
}


var clearSheet = function (e) {

    sName = e.target.parentNode.parentNode.children[0].innerText;
    var update = {
        "bookId": bId,
        "sheetName": sName
    }

    $.ajax({
        url: baseUrl + "clearSheet",
        method: 'PUT',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {
        clearCanvas();
    });
}

var duplicateSheet = function (e) {

    sName = e.target.parentNode.parentNode.children[0].innerText;
    var update = {
        "bookId": bId,
        "sheetName": sName
    }

    $.ajax({
        url: baseUrl + "copySheet",
        method: 'POST',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {

        $.get(baseUrl + "getSheets/" + bId, function (data) {
            $('#sheets').empty();
            //add sheet name to tabs
            var sheets = data['data']['sheets'];
            sName = sheets[0].name;

            // $('#sheets').empty();
            sheets.forEach(function (e) {

                console.log(e);

                $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
            });

            bindSheetListener();
            $("#tableName").text($('#book-selector').val());
            sheetData = data;

            openSheet(bId, sName, 0);

        })
    });

}

// var moveSheet = function(e, direction){
//
//     if (direction = "-1"){
//
//     }
//
//     sName = e.target.parentNode.parentNode.children[0].innerText;
//     var update = {
//       "bookId": bId,
//       "sheetName": sName,
//       "newPos":
//     }
//
//     $.ajax({
//       url: baseUrl+"moveSheet",
//       method:'PUT',
//       headers: {
//         'auth-token':'guest'
//       },
//       dataType:"json",
//       contentType:"application/json",
//       data: JSON.stringify(update)
//     })
//
// }

$("#addSheetButton").click(function () {
    $("#sheet-dialog").empty();
    $('<label></label>').text("Specify the name of the sheet(Unique in the book):").appendTo($("#sheet-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "createdSheetName").appendTo("#sheet-dialog");
    var dialog = $("#sheet-dialog").dialog({
        buttons: {
            "Create": function () {
                createSheet();
                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    })
})


var bindSheetListener = function () {

    $('.sheet>.btn.btn-secondary').click(function (e) {

        var target = e.target.parentNode.children[0]
        if ($(".btn-primary").length == 0) {
            $(target).attr('class', 'btn btn-primary');
            console.log("yes")
        } else {
            console.log("no")
            $(".btn-primary").attr('class', 'btn btn-secondary');
            $(target).attr('class', 'btn btn-primary');
        }

        sName = target.innerText;
        openSheet(bId, sName, 8)

    });

    $('.deleteSheet').click(function (e) {
        deleteSheet(e);
    });

    $(".renameSheet").click(function (e) {
        renameSheet(e);
    });

    $(".clearSheet").click(function (e) {
        clearSheet(e);
    })


    $(".moveLeftSheet").click(function (e) {

    })

    $(".moveRightSheet").click(function (e) {

    })

    $(".duplicateSheet").click(function (e) {
        duplicateSheet(e);
    })
}


var openSheet = function (bookId, sheetName, sheetIndex) {


    var r1 = 0;
    var c1 = 0;
    var r2 = 1000;
    var c2 = 50;
    sName = sheetName;

    $.get(baseUrl + "getCells/" + bookId + "/" + sheetName + "/" + r1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {

        // data['data']['cells'].forEach(function (e) {
        //     if (e.value !== 'null') {
        //         hot.setDataAtCell(e.row, e.col, e.value);
        //     }
        // })
        var testingarray = [];
        console.log(data)
        for (let i = r1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                if (data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value != "null") {
                    temp.push(data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value);
                } else {
                    temp.push("");
                }

            }
            testingarray.push(temp);
        }
        clearCanvas(testingarray);
    })

}


$("#rename-book").click(function () {
    renameBook();
});

$("#delete-book").click(function () {
    deleteBook();
});

$('#close-book').click(function () {
    closeBook();
});


$('#create-book').click(function () {
    $("#book-dialog").empty();

    $(function () {

        $('<label></label>').text("Create a new book:").appendTo($("#book-dialog"));
        $('<input type="text">').attr("value", '').attr("id", "createdBookName").appendTo("#book-dialog");

        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Create": function () {
                    var newBook = $('#createdBookName').val();
                    $.ajax({
                        url: baseUrl + "addBook",
                        method: "POST",
                        headers: {
                            "auth-token": "guest"
                        },
                        data: JSON.stringify({"name": newBook}),
                        dataType: "json",
                        contentType: "application/json"
                    }).done(function (result) {
                        clearCanvas();
                        $("#tableName").text(newBook);
                        bId = result['data']['book']['id'];
                        bName = result['data']['book']['name'];
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
});


var getBooks = function () {
    $.get(baseUrl + "getBooks", function (data) {
        // var i = 0;
        var workbooks = data['data']['books']
        workbooks.forEach(function (e) {
            $("<option></option>").text(e['name']).attr("id", e['id']).attr("link", e['link']).appendTo($('#book-selector'));
        });
        $('#book-selector').selectmenu();
        return data;
    })
}

$('#open-book').click(function () {

    $("#book-dialog").empty();
    $('<label></label>').text("Open a book:").appendTo($("#book-dialog"));
    $('<select></select>').attr("id", 'book-selector').appendTo($("#book-dialog"));

    $.get(baseUrl + "getBooks", function (data) {
        // var i = 0;
        var workbooks = data['data']['books']
        workbooks.forEach(function (e) {
            $("<option></option>").text(e['name']).attr("id", e['id']).attr("link", e['link']).appendTo($('#book-selector'));
        });
        $('#book-selector').selectmenu();
    })


    var dialog = $("#book-dialog").dialog({
        buttons: {
            "Confirm": function () {
                var bookName = $('#book-selector').prop('selectedIndex');
                var selectedBook = $('#book-selector>option')[bookName];

                var bookId = $(selectedBook).prop('id');

                bId = bookId;
                bName = $('#book-selector').val();


                $.get(baseUrl + "getSheets/" + bId, function (data) {
                    $('#sheets').empty();
                    //add sheet name to tabs
                    var sheets = data['data']['sheets'];
                    sName = sheets[0].name;

                    // $('#sheets').empty();
                    sheets.forEach(function (e) {

                        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

                    });

                    bindSheetListener();

                    $("#tableName").text($('#book-selector').val());
                    sheetData = data;

                    openSheet(bId, sName, 0);

                })

                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    });
});


var renameBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Rename the book:").appendTo($("#book-dialog"));
        $('<input type="text">').attr("value", bName).attr("id", "newBookName").appendTo("#book-dialog");
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Change Name": function () {
                    bName = $("#newBookName").val()
                    var update = {
                        "bookId": bId,
                        "newBookName": bName
                    }
                    $.ajax({
                        url: baseUrl + "changeBookName",
                        method: 'PUT',
                        headers: {
                            'auth-token': 'guest'
                        },
                        dataType: "json",
                        contentType: "application/json",
                        data: JSON.stringify(update)
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })

}

var deleteBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Are you sure you want to delete the book:" + bName).appendTo($("#book-dialog"));
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Delete": function () {
                    var update = {"bookId": bId};
                    $.ajax({
                        url: baseUrl + "deleteBook",
                        method: 'DELETE',
                        headers: {
                            'auth-token': 'guest'
                        },
                        dataType: "json",
                        contentType: "application/json",
                        data: JSON.stringify(update)
                    }).done(function (result) {
                        clearCanvas();
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}

var closeBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Are you sure you want to close the book:" + bName).appendTo($("#book-dialog"));
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Close": function () {
                    clearCanvas();
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}


// var customizeHeader = function(){
//   // select range
//   var selected = hot.getSelected();
//
//   var r1 = selected[0];
//   var c1 = selected[1];
//   var r2 = selected[2];
//   var c2 = selected[3];
//
//   var i;
//   var headerNames = []
//   for (i=c1; i <= c2; i++){
//     var header = hot.getDataAtCell(r1, i);
//     $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
//     $("<p style='display: inline;'></p>").text(header).appendTo($('#'+header));
//     $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#'+header));
//
//     // headerNames.push()
//   }
//   // click create Table
//   // change style(add a circle)
//   // change header cells
// };


var createTable = function (range) {
    var r1 = range[0];
    var c1 = range[1];
    var r2 = range[2];
    var c2 = range[3];
    var i;

    var headerNames = []

    $("#table-dialog").empty();
    $("#table-dialog").attr("title", "Create a table")
    $('<label></label>').text("Specify the name of the table:").appendTo($("#table-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "create-table-name").appendTo("#table-dialog");
    $('<label></label>').text("Specify the type of each column.").appendTo($("#table-dialog"));

    for (i = c1; i <= c2; i++) {
        var header = hot.getDataAtCell(r1, i);
        $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
        $("<p style='display: inline;'></p>").text(header).appendTo($('#' + header));
        $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#' + header));
    }
    // headerNames.push()

    // var headRow = $('.handsontable td.area').slice(c2-c1+1);
    // var bodyRows = $('.handsontable td.area').slice(0, c2-c1+1);
    // headRow.css("background","#2A7E43");
    // debugger
    // update spreadsheet setting
    hot.updateSettings({
        cells: function (row, col, prop) {
            var cellProperties;

            if (row === r1) {
                if (col >= c1 && col <= c2) {
                    // console.log(hot.getCellMeta(row, col));
                    var cell = hot.getCell(row, col);   // get the cell for the row and column
                    cell.style.backgroundColor = "#003F13";  // set the background color
                    cell.style.color = "#ffffff";
                }
            }

            if (row > r1 && row <= r2) {
                if (col >= c1 && col <= c2) {
                    var cell = hot.getCell(row, col);   // get the cell for the row and column
                    cell.style.backgroundColor = "#2A7E43";  // set the background color
                    cell.style.color = "#ffffff";
                }
            }

            return cellProperties;
        }
    });
    // bodyRows.css("background", "#003F13");

    // function firstRowRenderer(instance, td, row, col, prop, value, cellProperties) {
    //   Handsontable.renderers.TextRenderer.apply(this, arguments);
    //   td.style.fontWeight = 'bold';
    //   td.style.color = 'green';
    //   td.style.background = '#CEC';
    // }

    $(function () {

        var dialog = $("#table-dialog").dialog({
            buttons: {
                "Create Table": function () {
                    var tableName = $('#create-table-name').val();
                    var typeArray = []
                    $.each($("#table-dialog select"), function (index, value) {
                        typeArray.push($(value).val());
                    })

                    var table = {
                        'bookId': bId,
                        'sheetName': sName,
                        'tableName': tableName,
                        'row1': r1,
                        'col1': c1,
                        'row2': r2,
                        'col2': c2,
                        'schema': typeArray
                    }
                    console.log(table);

                    debugger

                    for (i = c1; i <= c2; i++) {
                        var header = hot.getDataAtCell(r1, i);
                        $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
                        $("<p style='display: inline;'></p>").text(header).appendTo($('#' + header));
                        $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#' + header));
                    }


                    // $.ajax({
                    //   url:baseUrl+"createTable",
                    //   method:"POST",
                    //   headers:{
                    //     "auth-token":"guest"
                    //   },
                    //   data:JSON.stringify(table),
                    //   dataType:"json",
                    //   contentType:"application/json"
                    // }).done(function(result){
                    // })


                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}


$('#createTable').click(function () {
    range = hot.getSelected();
    createTable(range);


    // $('.handsontable td.area').css("background-color", "#F00");

    // $(function(){
    //   customizeHeader();
    //   var dialog = $("#table-dialog").dialog({
    //     buttons: {
    //       "Create Table": function(){
    //         range = hot.getSelected();
    //         var row1 = range[0];
    //         var col1 = range[1];
    //         var row2 = range[2];
    //         var col2 = range[3];
    //         var tableName = $('#create-table-name').val();
    //
    //         var typeArray = []
    //         $.each( $("#table-dialog select"), function(index, value){
    //           typeArray.push($(value).val());
    //         })
    //
    //
    //         var table = {
    //           'bookId':bId,
    //           'sheetName':sName,
    //           'tableName':tableName,
    //           'row1': row1,
    //           'col1': col1,
    //           'row2':row2,
    //           'col2':col2,
    //           'schema':typeArray
    //         }
    //
    //         $.ajax({
    //           url:baseUrl+"createTable",
    //           method:"POST",
    //           headers:{
    //             "auth-token":"guest"
    //           },
    //           data:JSON.stringify(table),
    //           dataType:"json",
    //           contentType:"application/json"
    //         }).done(function(result){
    //         })
    //
    //
    //
    //         dialog.dialog('close');
    //       },
    //       Cancel: function(){
    //         dialog.dialog('close');
    //       }
    //     }
    //   })
    // })

});

$(".fa.fa-underline").click(function () {
    $(".handsontable td.area").css("text-decoration", "underline");
})

$(".fa.fa-strikethrough").click(function () {
    $(".handsontable td.area").css("text-decoration", "line-through");
})

$(".fa.fa-bold").click(function () {
    $(".handsontable td.area").css("font", "bold");
})

$(".fa.fa-italic").click(function () {
    $(".handsontable td.area").css("font", "italic");
})


$(".fa.fa-font").click(function () {
    var color = $(".jscolor").val();
    $(".handsontable td.area").css("color", "#" + color);
})

$(".fa.fa-paint-brush").click(function () {
    // console.log();
    var color = $(".jscolor").val();
    $(".handsontable td.area").css("background-color", "#" + color);
    // changeBackgroundColor();
})

$(document).ready(
    function () {
        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">Untitled</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

        sheetNames.push('Untitled');
        bindSheetListener();
    }
);

var bookDialog = $("#book-dialog");
$("#importTable").click(function () {
    bookDialog.empty();
    $(function () {
        $("<input type='file' name='File Upload' id='txtFileUpload' accept='.csv' />").appendTo(bookDialog);
        var dialog = bookDialog.dialog({
            buttons: {
                Confirm: function () {
                    var uploadedFile = $("#txtFileUpload").prop('files')[0];
                    if (uploadedFile != undefined) {
                        console.log(uploadedFile);
                        var filereader = new FileReader();
                        filereader.readAsArrayBuffer(uploadedFile);
                        filereader.onloadend = function (e) {
                            console.log(filereader.result);
                            var bytes = new Uint8Array(filereader.result);
                            console.log(bytes)
                            $.ajax({
                                url: baseUrl + "importBook",
                                method: "POST",
                                headers: {
                                    "auth-token": "guest"
                                },
                                data: bytes,
                                processData: false,
                                contentType: 'application/octet-stream',
                            }).done(function (result) {
                                console.log(result);
                                // $("#tableName").text(newBook);
                                // bId = result['data']['book']['id'];
                                // bName = result['data']['book']['name'];
                                if (result.status == "success") {
                                    bId = result.data.book.id;
                                    //sName = result.data.sheetName;
                                    $.get(baseUrl + "getSheets/" + bId, function (data) {
                                        $('#sheets').empty();
                                        //add sheet name to tabs
                                        var sheets = data['data']['sheets'];
                                        sName = sheets[0].name;

                                        // $('#sheets').empty();
                                        sheets.forEach(function (e) {

                                            $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

                                        });

                                        bindSheetListener();

                                        $("#tableName").text($('#book-selector').val());
                                        sheetData = data;

                                        importSheet(bId, sName, 0);

                                    })
                                    dialog.dialog('close');
                                } else {
                                    alert(result.message);
                                }

                            })

                        };
                    } else {
                        alert("file not uploaded");
                    }


                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
});


var importSheet = function (bookId, sheetName, sheetIndex) {

    var r1 = 0;
    var c1 = 0;
    var r2 = 1000;
    var c2 = 50;
    sName = sheetName;

    $.get(baseUrl + "getCells/" + bookId + "/" + sheetName + "/" + r1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {
        //Todo check return success or failure
        var testingarray = [];
        console.log(data)
        for (let i = r1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                if (data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value != "null") {
                    temp.push(data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value);
                } else {
                    temp.push("");
                }

            }
            testingarray.push(temp);
        }
        clearCanvas(testingarray);
    })

}

$(window).resize(function () {
    console.log("resized")
    wrapperHeight = $(".wrapper").height();
    wrapperWidth = $(".wrapper").width();
    if (exploreOpen) {
        hot.updateSettings({
            width: wrapperWidth * 0.8,
            height: wrapperHeight * 0.95,
        });
        nav.updateSettings({
            width: wrapperWidth * 0.19,
            height: wrapperHeight * 0.95,
        })

    } else {
        hot.updateSettings({
            width: $(".wrapper").width(),
            height: $(".wrapper").height(),
        });
    }
});

var updateData = function (r1, c1, r2, c2, scrollTo) {
    //   clearCanvas();

    //sName = sheetName;
    if (r1 < 0) {
        r1 = 0;
    }
    let temp1 = ((r1 - 30) < 0) ? r1 : r1 - 30;


    $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + temp1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {

        // data['data']['cells'].forEach(function(e){
        //   if(e.value!=='null'){
        //     hot.setDataAtCell(e.row, e.col, e.value);
        //   }
        // })
        var testingarray = [];
        for (let i = temp1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                temp.push(data.data.cells[(i - temp1) * (c2 - c1 + 1) + (j - c1)].value);
            }
            testingarray.push(temp);
        }
        // for(let i = 0; i < 100; i++){
        //   for (let j = 0; j < 10; j++){
        //      hot.setDataAtCell(i, j, testingarray[i][j]);
        //   }
        // }
        hot.populateFromArray(temp1, c1, testingarray);
        if (scrollTo) {
            hot.scrollViewportTo(r1);
        }
    })

}



// import handlebars;
var navContainer = document.getElementById('navChart');
var currLevel = 0;
var levelList = [];
var spanList = [];
var cumulativeData = [];
var viewData;
// for (let i = 0; i < 11; i++){
//   viewData[i] = [""];
// }
var mergeCellInfo = [];
var colHeader = [];
var cumulativeDataSize = 0;
var nav;
var clickable = true;

var options = [];
var hieraOpen = false;
var exploreOpen = false;
var attr_index = [];
var funcId = [];

//var agg_id_ls = "";
var funcOptions = ["AVEDEV", "AVERAGE", "COUNT", "COUNTA", "COUNTBLANK", "COUNTIF", "DEVSQ", "LARGE", "MAX", "MAXA", "MIN", "MINA", "MEDIAN", "MODE", "RANK", "SMALL", "STDEV", "SUBTOTAL", "SUM", "SUMIF", "SUMSQ", "VAR", "VARP"]
var subtotalFunc = ["AVERAGE", "COUNT", "COUNTA", "MAX", "MIN", "PRODUCT", "STDEV", "SUM", "VAR", "VARP"];

var currData;
//var zoomOutOn = true;
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

var exploreAttr;

var sortAttrIndices = [];

var selectedChild = 0;
var lowerRange;
var upperRange;

var currRange;


// first step start showing navigation options
// $("#navigationPanel").click(function () {
//     console.log("2")
//     // updateData(0,0,1000,15);
//     lowerRange = 0;
//     upperRange = 1000;
//     $("#explorationtool-bar").css("display", "inline");
//
//     $.get(baseUrl + 'getSortAttrs/' + bId + '/' + sName, function (data) {
//         var $dropdown = $("#exploreOpt");
//         options = data.data
//         console.log(options)
//         for (let i = 0; i < options.length; i++) {
//             let tempString = "<div class='form-check'>" +
//                 "<input class='form-check-input' " +
//                 "type='radio' name='exploreValue' id='Radios" + i + "' value='" + (i + 1) + "'>" +
//                 "<label class='form-check-label' for='Radios" + i + "'> " +
//                 options[i] + "</label></div>"
//             $dropdown.append(tempString);
//         }
//         var $aggregateCol = $("#aggregateCol");
//         $aggregateCol.append(createAggreString());
//
//         $("#aggregateOpt0").change(function () {
//             // Do something with the previous value after the change
//             $(this).nextAll().remove();
//             switch (this.value) {
//                 case "COUNTIF":
//                 case "SUMIF":
//                     $(this).after("<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
//                     break;
//                 case "LARGE":
//                 case "SMALL":
//                     $(this).after("<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
//                     break;
//                 case "SUBTOTAL":
//                     let tempString = "<select class='' id='aggrePara0'><option value='' disabled selected hidden>Function_num</option>";
//                     for (let i = 0; i < subtotalFunc.length; i++) {
//                         tempString += "<option value='" + (i + 1) + "''>" + subtotalFunc[i] + "</option>";
//                     }
//                     tempString += "</select>";
//                     $(this).after(tempString);
//                     break;
//                 case "RANK":
//                     tempString = "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>";
//                     tempString += "<select class='' id='aggrePara00'><option value='0' selected >ascending</option><option value='1'>descending</option></select>"
//                     $(this).after(tempString);
//                     break;
//             }
//         });
//
//         var $sortDropdown = $("#inlineOpt");
//         $sortDropdown.append(createSortString());
//
//     });

//showing exploration options and create corresponding html
$("#Explore").click(function () {
    lowerRange = 0;
    upperRange = 1000;

    $.get(baseUrl + 'getSortAttrs/' + bId + '/' + sName, function (data) {
        var $dropdown = $("#exploreOpt");
        $dropdown.empty();
        $dropdown.append("<legend class=\"form-label\" style=\"font-size:1rem\">Attribute Name</legend>\n");
        options = data.data
        console.log(options)
        for (let i = 0; i < options.length; i++) {
            let tempString = "<div class='form-check'>" +
                "<input class='form-check-input' " +
                "type='radio' name='exploreValue' id='Radios" + i + "' value='" + (i + 1) + "'>" +
                "<label class='form-check-label' for='Radios" + i + "'> " +
                options[i] + "</label></div>"
            $dropdown.append(tempString);
        }

        // clear cumulative string for aggregate attribute, function and sortattribute
        aggregateColStr = "";
        aggregateOptStr = "";
        sortOptionString = "";

        var $aggregateCol = $("#aggregateCol");
        $aggregateCol.empty();
        aggregateTotalNum = 0
        $aggregateCol.append(createAggreString());

        //customize input field for different formula
        $("#aggregateOpt0").change(function () {
            // Do something with the previous value after the change
            $(this).nextAll().remove();
            switch (this.value) {
                case "COUNTIF":
                case "SUMIF":
                    $(this).after("<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
                    break;
                case "LARGE":
                case "SMALL":
                    $(this).after("<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>");
                    break;
                case "SUBTOTAL":
                    let tempString = "<select class='' id='aggrePara0'><option value='' disabled selected hidden>Function_num</option>";
                    for (let i = 0; i < subtotalFunc.length; i++) {
                        tempString += "<option value='" + (i + 1) + "''>" + subtotalFunc[i] + "</option>";
                    }
                    tempString += "</select>";
                    $(this).after(tempString);
                    break;
                case "RANK":
                    tempString = "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara0'>";
                    tempString += "<select class='' id='aggrePara00'><option value='0' selected >ascending</option><option value='1'>descending</option></select>"
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

//});

function createAggreString() {
    let tempString = "<div><select class='custom-select my-1' id='aggregateCol" + aggregateTotalNum + "''><option value='' disabled selected hidden>Attribute" + aggregateTotalNum + "</option>";
    if (aggregateTotalNum == 0) {
        for (let i = 0; i < options.length; i++) {
            aggregateColStr += "<option value='" + (i + 1) + "''>" + options[i] + "</option>";
        }
        aggregateColStr += "</select>";
        tempString += aggregateColStr;
    } else {
        tempString += aggregateColStr;
    }

    tempString += "<select class='custom-select my-1 ' id='aggregateOpt" + aggregateTotalNum + "''><option value='' disabled selected hidden>Function" + aggregateTotalNum + "</option>";
    if (aggregateTotalNum == 0) {
        for (let i = 0; i < funcOptions.length; i++) {
            aggregateOptStr += "<option value='" + funcOptions[i] + "''>" + funcOptions[i] + "</option>";
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
        console.log(e)
        let number = e.target.id.charAt(e.target.id.length - 1)
        console.log(number)

        // Do something with the previous value after the change
        $(this).nextAll().remove();
        switch (this.value) {
            case "COUNTIF":
            case "SUMIF":
                $(this).after("<span>Predicate:&nbsp</span><input class='' type='text' name='' id='aggrePara" + number + "'>");
                break;
            case "LARGE":
            case "SMALL":
                $(this).after("<span>Int:&nbsp</span><input class='' type='text' name='' id='aggrePara" + number + "'>");
                break;
            case "SUBTOTAL":
                let tempString = "<select class='' id='aggrePara" + number + "'><option value='' disabled selected hidden>Function_num</option>";
                for (let i = 0; i < subtotalFunc.length; i++) {
                    tempString += "<option value='" + (i + 1) + "''>" + subtotalFunc[i] + "</option>";
                }
                tempString += "</select>";
                $(this).after(tempString);
                break;
            case "RANK":
                tempString = "<span>Value:&nbsp</span><input class='' type='text' name='' id='aggrePara" + number + "'>";
                tempString += "<select class='' id='aggrePara" + number + number + "'><option value='0' selected >ascending</option><option value='1'>descending</option></select>"
                $(this).after(tempString);
                break;
        }
    });
})

$("#aggreRemove").click(function () {
    console.log("remove")

    if (aggregateTotalNum > 1) {
        $("#aggregateCol").children().last().remove();
        aggregateTotalNum -= 1;
    }
})

// create sorting html code
function createSortString() {
    let tempString = "<label class='my-1 mr-5' for='inlineOpt" + sortTotalNum + "'>Attribute</label><select class='custom-select my-1 mr-xl-5' id='inlineOpt" + sortTotalNum + "''> ";
    if (sortTotalNum == 0) {
        for (let i = 0; i < options.length; i++) {
            sortOptionString += "<option value='" + (i + 1) + "''>" + options[i] + "</option>";
        }
        sortOptionString += "</select>";
        tempString += sortOptionString;
    } else {
        tempString += sortOptionString;
    }
    sortTotalNum += 1;
    console.log(tempString)
    return tempString;
}

// adding a new attribute for sort pop-up menue
$("#sortAdd").click(function () {
    var $sortDropdown = $("#inlineOpt");
    $sortDropdown.append(createSortString());
})

$("#sortRemove").click(function () {
    console.log("remove")
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
    console.log(this)
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

//second step  navigation start, showing left colum.
function Explore(e) {
    $("#navPath").css({"display": "block", "height": "5%"});
    $("#Hierarchical").click(function () {
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
        console.log(selectedArray)
        if (selectedArray && selectedArray.length == 1) {
            $("#Sort").css({"display": "block"})
        } else {
            $("#Sort").css({"display": "none"})
        }
    })

    $("#test-hot").css({"float": "left"});
    $("#navChart").css({"display": "inline", "float": "left"});


    $.get(baseUrl + 'startNav/' + bId + '/' + sName + '/' + e, function (data) {
        clickable = true;
        currLevel = 0;
        levelList = [];
        spanList = [];
        cumulativeData = [];

        mergeCellInfo = [];
        colHeader = [options[e - 1]];
        console.log(colHeader)
        cumulativeDataSize = 0;

        var result = JSON.parse(data.data);
        currData = result.data;
        currRange = currData[currData.length - 1].rowRange[1] - currData[0].rowRange[0];
        console.log(currData);


        cumulativeData.push(currData);
        viewData = new Array(currData.length);
        // for (let i = 0; i < currData.length; i++){
        //   console.log(i);
        //   viewData[mergeCellInfo[i].row][currLevel]= cumulativeData[0][i].name;
        // }
        console.log(viewData)
        for (let i = 0; i < currData.length; i++) {
            viewData[i] = [""];
        }
        for (let i = 0; i < currData.length; i++) {
            console.log(i);
            viewData[i][0] = cumulativeData[0][i].name;
        }

        cumulativeDataSize += currData.length;

        hot.updateSettings({
            width: wrapperWidth * 0.8,
            height: wrapperHeight * 0.95,
        });


        //default setting
        var navSettings = {
            //  minRows: testData.length,

            // minRows: currData.length,
            //   maxRows:11,
            minCols: 1,
            // maxCols:1,
            //autoColumnSize : true,
            readOnly: true,
            rowHeights: (wrapperHeight * 0.95 / currData.length > 80) ? wrapperHeight * 0.95 / currData.length : 80,
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
                                return colHeader[col] + "<span id='colClose' >x</span>";
                        }
                    } else {
                        switch (col) {
                            case 0:
                                return colHeader[0];
                            case 1:
                                return colHeader[1];
                            default:
                                return colHeader[col] + "<span id='colClose'>x</span>";
                        }
                    }

                }
            },
            // colHeaders: colHeader,
            //fixedRowsTop: 11,
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
                console.log(e)
                console.log(element)
                console.log(coords);
                $("#formulaBar").val("");

                let topLevel = (currLevel == 0 && coords.col != 0)
                let otherLevel = (currLevel > 0 && coords.col != 1)
                if (topLevel && coords.row >= 0) {
                    $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 1]);
                } else if (currLevel > 0 && coords.row >= 0 && coords.col >= 2) {
                    $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 2]);
                }


                if (topLevel || otherLevel || zoomming || e.realTarget.className == "colHeader" || e.realTarget.className == "relative") {
                    e.stopImmediatePropagation();
                }
                if (e.realTarget.id == "colClose") {
                    removeHierarchiCol(coords.col)
                    console.log(colHeader);
                    console.log(viewData)
                }
            },
            afterSelection: function (r, c, r2, c2, preventScrolling, selectionLayerLevel) {
                // setting if prevent scrolling after selection
                console.log(r)


                if (cumulativeData[currLevel][r] != undefined) {
                    selectedChild = r;
                    lowerRange = cumulativeData[currLevel][r].rowRange[0];
                    upperRange = cumulativeData[currLevel][r].rowRange[1];
                    updateData(cumulativeData[currLevel][r].rowRange[0], 0, cumulativeData[currLevel][r].rowRange[1], 15, true);
                    console.log(upperRange)
                    nav.render();
                }
                // hot.scrollViewportTo(cumulativeData[currLevel][r/spanList[currLevel]].rowRange[0]);
                // hot.scrollViewportTo(cumulativeData[currLevel][r/spanList[currLevel]].rowRange[0]);

            },

            data: viewData,
            //  doubleclick implementation option1:
            //  afterOnCellMouseDown: function(event, cell, td) {
            //   var now = new Date().getTime();
            //    // check if dbl-clicked within 1/5th of a second. change 200 (milliseconds) to other value if you want
            //   if(!(td.lastClick && now - td.lastClick < 200)) {
            //     td.lastClick = now;
            //     return; // no double-click detected
            //   }
            //  console.log(cell)
            //   // double-click code goes here
            //   if(currLevel == 0){
            //        if(clickable && cell.col == 0){
            //          var child = cell.row/spanList[currLevel];
            //          console.log(child);
            //          nav.deselectCell();
            //          zoomIn(child,nav);
            //        }
            //      }else{
            //        if(clickable && cell.col == 1){
            //          var child = cell.row/spanList[currLevel];
            //          console.log(child);
            //          nav.deselectCell();
            //          zoomIn(child,nav);
            //        }else if(cell.col == 0){
            //          zoomOut(nav);
            //        }
            //      }
            //   console.log('double clicked');
            // },
            cells: function (row, column, prop) {
                let cellMeta = {}
                if (currLevel == 0) {
                    if (column == 0 && row == selectedChild) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#D3D3D3';
                            td.style.color = 'white';
                        }
                    } else if (column == 0) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#F5F5DC';
                        }
                    } else {
                        cellMeta.renderer = chartRenderer;
                        //     function (hotInstance, td, row, col, prop, value, cellProperties) {
                        //     Handsontable.renderers.TextRenderer.apply(this, arguments);
                        //     td.style.background = '#FAEBD7';
                        // }
                    }
                } else {
                    if (column == 1 && row == selectedChild) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#D3D3D3';
                            td.style.color = 'white';
                        }
                    } else if (column <= 1) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#F5F5DC';
                        }
                    } else {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                            Handsontable.renderers.TextRenderer.apply(this, arguments);
                            td.style.background = '#FAEBD7';
                        }
                    }
                }
                return cellMeta;
            }
        }
        // //initializing interface
        navContainer.innerHTML = ""
        nav = new Handsontable(navContainer, navSettings);
        nav.selectCell(0, 0);


        console.log(viewData);
        updateData(0, 0, 1000, 15, true);
        lowerRange = 0;
        upperRange = 1000;
        updataHighlight();


        //   doubleclick implementation option2:
        nav.view.wt.update('onCellDblClick', function (e, cell) {

            console.log("double")
            console.log(cell);
            if (cell.row >= 0) {
                if (currLevel == 0) {
                    if (cell.col == 0 && cumulativeData[currLevel][cell.row].clickable) {
                        //        var child = cell.row/spanList[currLevel];
                        var child = cell.row;
                        console.log(child);
                        nav.deselectCell();
                        zoomming = true;
                        zoomIn(child, nav);
                    }
                } else {
                    if (cell.col == 1 && cumulativeData[currLevel][cell.row].clickable) {
                        //  var child = cell.row/spanList[currLevel];
                        var child = cell.row;
                        console.log(child);
                        nav.deselectCell();
                        zoomming = true;
                        zoomIn(child, nav);
                    } else if (cell.col == 0) {
                        zoomouting = true;
                        zoomOut(nav);
                    }
                }
            }

        });

    });
}


function removeHierarchiCol(colIdx) {
    colHeader.splice(colIdx, 1,)
    if (currLevel == 0) {
        aggregateData.formula_ls.splice(colIdx - 1, 1,);
    } else {
        aggregateData.formula_ls.splice(colIdx - 2, 1,);
    }
    if (aggregateData.formula_ls.length == 0) {
        hieraOpen = false;
    }
    nav.alter('remove_col', colIdx);
    console.log(viewData)

    //nav.render();
}

$("#hierarchi-form").submit(function (e) {
    aggregateData.bookId = bId;
    aggregateData.sheetName = sName;
    e.preventDefault();
    aggregateData.formula_ls = [];
    // attr_index = []
    // funcId = []
    hieraOpen = false;
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
                console.log(para)
                if (para == "") {
                    alert("Predicate is empty");
                    return;
                } else {
                    paras = ["", para]
                    aggregateData.formula_ls[i] = {attr_index: attrIdx, function: funct, param_ls: paras};
                }
                ;
                break;
            case "LARGE":
            case "SMALL":
                para = $("#aggrePara" + i).val();
                console.log(para)
                if (para == "") {
                    alert("int value is empty");
                    return;
                } else {
                    paras = ["", para]
                    aggregateData.formula_ls[i] = {attr_index: attrIdx, function: funct, param_ls: paras};
                }
                ;
                break;
            case "SUBTOTAL":
                para = $("#aggrePara" + i).val();
                console.log(para)
                if (para == null) {
                    alert("function ID is empty");
                    return;
                } else {
                    paras = [para, ""]
                    aggregateData.formula_ls[i] = {attr_index: attrIdx, function: funct, param_ls: paras};
                }
                ;
                break;
            case "RANK":
                para = $("#aggrePara" + i).val();
                console.log(para)
                if (para == "") {
                    alert("Rank value is empty");
                    return;
                } else {
                    console.log($("#aggrePara" + i + i).val());
                    paras = [para, "", $("#aggrePara" + i + i).val()]
                    aggregateData.formula_ls[i] = {attr_index: attrIdx, function: funct, param_ls: paras};
                }
                ;
                break;
            default:
                aggregateData.formula_ls[i] = {attr_index: attrIdx, function: funct, param_ls: [""]};
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
        //dataType: 'json',
        contentType: 'text/plain',
        data: JSON.stringify(aggregateData),
    }).done(function (e) {
        if (e.status == "success") {
            $("#hierarchical-col").css("display", "none");
            hot.updateSettings({width: wrapperWidth * 0.8});
            hieraOpen = true;
            if (currLevel == 0) {
                colHeader.splice(1, colHeader.length - 1,);
                for (let i = 0; i < viewData.length; i++) {
                    viewData[i].splice(1, viewData[i].length - 1,)
                }
            } else {
                colHeader.splice(2, colHeader.length - 2,);
                for (let i = 0; i < viewData.length; i++) {
                    viewData[i].splice(2, viewData[i].length - 2,)
                }
            }
            for (let i = 0; i < e.data.length; i++) {
                let hierCol = aggregateData.formula_ls[i];
                console.log(aggregateData.formula_ls[i])
                colHeader.push(options[hierCol.attr_index - 1] + " " + hierCol.function + " " + hierCol.param_ls);
            }
            console.log(e)
            addHierarchiCol(e.data);
        } else {
            alert("There is some problem with the formula: " + e.message);
        }
    })

}


function addHierarchiCol(aggregateValue) {
    console.log(nav.getRowHeight(0))
    console.log(viewData);
    console.log(aggregateValue);
    let targetCol = (currLevel == 0) ? 1 : 2;
    console.log(cumulativeData[currLevel])
    navRawFormula = [];
    for (let i = 0; i < cumulativeData[currLevel].length; i++) {
        let formulaRow = [];
        for (let j = 0; j < aggregateValue.length; j++) {
            //  if( i == cumulativeData[currLevel].length - 1){
            //     mergeCellInfo.push({row: i*spanList[currLevel], col: targetCol + j, rowspan: 11 - i*spanList[currLevel], colspan: 1});
            //  }else{
            //     mergeCellInfo.push({row: i*spanList[currLevel], col: targetCol + j, rowspan: spanList[currLevel], colspan: 1});
            //  }
            // viewData[i*spanList[currLevel]][targetCol+j]= aggregateValue[j].list[i].toFixed(2);
            console.log(i)
            console.log(j)
            formulaRow.push(aggregateValue[j][i].formula);
            let text = aggregateValue[j][i].value;
            if (isNaN(text)) {
                viewData[i][targetCol + j] = text;
            } else {
                viewData[i][targetCol + j] = text.toFixed(2);
            }
            console.log("start" + i)
            console.log(targetCol + j)
        }
        navRawFormula.push(formulaRow);
    }
    console.log(navRawFormula);
    console.log(viewData)
    let columWidth = [];
    if (currLevel >= 1) {
        columWidth = [, ,];
    } else {
        columWidth = [,];
    }
    for (let j = 0; j < aggregateValue.length; j++) {
        columWidth.push(wrapperWidth * 0.14);
    }
    console.log(columWidth)

    let numChild = cumulativeData[currLevel].length;
    nav.updateSettings({
        manualColumnResize: columWidth,
        minCols: 1,
        data: viewData,
        rowHeights: (wrapperHeight * 0.95 / numChild > 80) ? wrapperHeight * 0.95 / numChild : 80,

        //maxCols: 3,
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
    let childlist = computePath();


    $.get(baseUrl + 'getChildren/' + bId + '/' + sName + '/' + childlist, function (data) {

        console.log(data);
        var result = JSON.parse(data.data);
        //clickable = result.clickable;
        console.log(result)
        currLevel += 1;
        currData = result.data;
        mergeCellInfo = [];
        mergeCellInfo.push({row: 0, col: 0, rowspan: currData.length, colspan: 1});


        viewData = new Array(currData.length);
        for (let i = 0; i < currData.length; i++) {
            if (i == 0) {
                viewData[i] = [cumulativeData[currLevel - 1][child].name];
            } else {
                viewData[i] = [""];
            }
        }


        // spanList.push(span);
        console.log(mergeCellInfo)

        cumulativeData.push(currData);

        console.log(cumulativeData);


        // for (let i = 0; i < currData.length; i++){
        //
        //    //double layer
        //     viewData[i*span][1]= cumulativeData[currLevel][i].name;
        //
        //  }

        for (let i = 0; i < currData.length; i++) {
            //double layer
            viewData[i][1] = cumulativeData[currLevel][i].name;

        }


        cumulativeDataSize += currData.length;

        let columWidth = [];
        if (currLevel >= 1) {
            //columWidth = [50];
        } else {
            columWidth = 200;
        }
        // console.log(nav.getColHeader())
        // console.log(viewData);
        // console.log(nav.getColWidth(1))
        // console.log(nav.getCopyableText(0,0,10,2))
        // nav.render();

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
            nav.selectCell(0, 1)
        }
        updateNavPath();
        // zoomming = false;
        //  nav.selectCell(0, 1)
        //  nav.render();
    });


}

function updateNavPath() {
    let $breadcrumbList = $(".breadcrumb");
    $breadcrumbList.empty();
    let tempString = "";
    if (currLevel > 0) {
        tempString = "<li class='breadcrumb-item'><a href='#' id='0'>TopLevel</a></li>";
        for (let i = 0; i < levelList.length - 1; i++) {
            tempString += "<li class='breadcrumb-item'> <a href='#' id='" + (i + 1) + "'>" + cumulativeData[i][levelList[i]].name + "</a></li>";
        }
        tempString += "<li class='breadcrumb-item active' aria-current='page'>" + cumulativeData[currLevel - 1][levelList[currLevel - 1]].name + "</li>";
    } else {
        tempString = "<li class='breadcrumb-item' aria-current='page'>TopLevel</li>"
    }
    $breadcrumbList.append(tempString);
    $(".breadcrumb-item a").click(function (e) {
        console.log(e.target.id);
        for (let i = e.target.id; i < currLevel;) {
            zoomouting = true;
            zoomOut(nav);
        }
    });
}


function zoomOut(nav) {
    clickable = true;
    nav.deselectCell();

    //api call to /levelList + '.' + child to get currData


    //spanList.pop();
    let numChild = cumulativeData[currLevel].length;
    currLevel -= 1;
    cumulativeData.pop();
    targetChild = levelList[levelList.length - 1]
    levelList.pop();
    cumulativeDataSize -= numChild;
    // mergeCellInfo.splice(-numChild,numChild);
    //console.log(mergeCellInfo)
    console.log(cumulativeDataSize)
    console.log(cumulativeData);
// for (let i = 0; i < numChild; i++){
//   viewData[i].pop();
// }
// if(hieraOpen){
//   for (let i = 0; i < attr_index.length; i++){
//     for (let j = 0; j < numChild; j++){
//       viewData[j].pop();
//     }
//   }
// }

    console.log(levelList)
    numChild = cumulativeData[currLevel].length;
    viewData = new Array(numChild);
    mergeCellInfo = [];
    if (currLevel > 0) {
        mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});
        for (let i = 0; i < numChild; i++) {
            if (i == 0) {
                viewData[i] = [cumulativeData[currLevel - 1][levelList[currLevel - 1]].name];
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

    console.log(viewData);

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
            rowHeights: (wrapperHeight * 0.95 / numChild > 80) ? wrapperHeight * 0.95 / numChild : 80,
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


    //nav.render();
}

$("#sort-form").submit(function (e) {
    e.preventDefault();
    $("#exampleModal").modal('hide')
    sortAttrIndices = [];
    for (let i = 0; i < sortTotalNum; i++) {
        sortAttrIndices.push($('#inlineOpt' + i).val());
    }
    var selectedArray = nav.getSelected();
    console.log(selectedArray)
//  var child = selectedArray[0][0]/spanList[currLevel];
    var child = selectedArray[0][0]
    console.log(child);
    let childlist = computePath();
    let path = ""
    if (childlist !== "") {
        path += childlist + ',' + child
    } else {
        path = child
    }

    $.get(baseUrl + 'sortBlock/' + bId + '/'
        + sName + '/ ' + path + '/' + sortAttrIndices + '/' + 0, function (data) {
        console.log(data);
        updateData(cumulativeData[currLevel][child].rowRange[0], 0, cumulativeData[currLevel][child].rowRange[1] + 10, 15, true)

        console.log(cumulativeData[currLevel][child].rowRange[0]);
        updataHighlight(child);

    });

})


function chartRenderer(instance, td, row, col, prop, value, cellProperties) {
    if (currLevel == 0) {
        if (navRawFormula[row][col - 1].includes("AVERAGE")) {
            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";
            console.log(td.innerHTML)
            let special = 3;
            var distribution = [{
                boundary: "1-10",
                count: 80,
            }, {
                boundary: "10-20",
                count: 100,
            }, {
                boundary: "20-30",
                count: 30,
            }, {
                boundary: "30-40",
                count: 50,
            }, {
                boundary: "40-50",
                count: 70,
            },
                //   {temp: 83, month: 'Auguest'},
            ];
            var boundaries = distribution.map(function (t) {
                return t.boundary
            });

            var margin = {top: 20, right: 15, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            var fullWidth = wrapperWidth * 0.14;
            var fullHeight = nav.getRowHeight(row);
            console.log(nav.getRowHeight(0))
            console.log(nav.getRowHeight(1))
            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString).append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

            var colors = d3.scaleLinear()
                .domain([130, 200])
                .range(d3.schemeYlGn);


            svg.append("rect")
                .attr("x", width + margin.right / 10)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(((value - 129.28) * 0.85 + 0.15) / (193.22 - 129.28)))

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", 0 - (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text("AVG:" + value);

            // // x value determined by month
            // var boundScale = d3.scaleBand()
            //     .domain([0,100])
            //     .range([0, width])
            //     .paddingInner(0.1);
            // // the width of the bars is determined by the scale
            // var bandwidth = boundScale.bandwidth();

            let xScale = d3.scaleLinear()
                .domain([0,200])
                .range([0, width]);

            // y value determined by temp
            var maxValue = d3.max(distribution, function (d) {
                return d.count;
            });
            var yScale = d3.scaleLinear()
                .domain([0, maxValue])
                .range([height, 0])
                .nice();

            var xAxis = d3.axisBottom(xScale)
                //.ticks(6,'s');
                .tickValues([0, 40,80, 120, 160,200]);


            var yAxis = d3.axisLeft(yScale);
            yAxis.ticks(4);

            var barHolder = svg.append('g')
                .classed('bar-holder', true);

            var tooltip = d3.select('#' + tempString).append("div")
                .attr("class", "toolTip");


            // draw the bars
             var bars = barHolder.selectAll('rect.bar')
                 .data(distribution)
                 .enter().append('rect')
                 .classed('bar', true)
                 .attr('x', function (d, i) {
                     // the x value is determined using the
                     // month of the datum
                     return 1 + width/5*i;
                 })
                 .attr('width', width/5)
                 .attr('y', function (d) {
                     return yScale(d.count);
                 })
                 .attr('fill', function (d, i) {
                     if (i == special) {
                         return '#ffa158'
                     } else {
                         return '#0099ff';
                     }
                 })
                 .attr('height', function (d) {
                     // the bar's height should align it with the base of the chart (y=0)
                     return height - yScale(d.count);
                 })
                 .on("mouseover", function(d) {
                     tooltip
                         .style("left", d3.event.pageX- 20  + "px")
                         .style("top", d3.event.pageY + "px")
                         .style("display", "inline-block")
                         .html((d.boundary));
                 })
                 .on("mouseout", function(d) {
                     tooltip.style("display", "none");
                 });

            // draw the axes
            svg.append('g')
                .classed('x axis', true)
                .attr('transform', 'translate(0,' + height + ')')
                .call(xAxis);

            var yAxisEle = svg.append('g')
                .classed('y axis', true)
                .call(yAxis);



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


        } else {
            Handsontable.renderers.TextRenderer.apply(this, arguments);
        }
    } else {

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
                cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
                    Handsontable.renderers.TextRenderer.apply(this, arguments);
                    td.style.background = '#CEC';
                }
            }
            if (child != undefined) {
                let lower = cumulativeData[currLevel][child].rowRange[0];
                let upper = cumulativeData[currLevel][child].rowRange[1];
                for (let i = 0; i < sortTotalNum; i++) {
                    if (column == (sortAttrIndices[i] - 1) && row >= lower && row <= upper) {
                        cellMeta.renderer = function (hotInstance, td, row, col, prop, value, cellProperties) {
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