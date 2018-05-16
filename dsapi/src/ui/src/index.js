// import React from 'react';
// import ReactDOM from 'react-dom';
// import HotTable from 'react-handsontable';
import Handsontable from 'handsontable'
// import handlebars;
var container = document.getElementById('test-hot');

// var searchFiled = document.getElementById('search_field');
var data;
var selectedRange;

// mock service for getting data
var fetchData = function(n) {
    return Handsontable.helper.createSpreadsheetData(n, 20);
};

var testData =fetchData(1000);

var compute_window = function(e) {
    var rowCount = hot.countRows();
    var rowOffset = hot.rowOffset();
    var visibleRows = hot.countVisibleRows();
    var lastRow = rowOffset + (visibleRows * 1);
    var lastVisibleRow = rowOffset + visibleRows + (visibleRows/2);
    var threshold = 15;

    console.log(rowCount);

    if(lastVisibleRow > (rowCount - threshold)) {
        loadMoreData(rowCount);
        console.log("in compute window");
    }
};

// load data and render
var loadMoreData = function(n) {
    // call data service
    var incoming = fetchData(n);
    var emptyArray = Array(50).fill(null);

    incoming.forEach(function(d) {
        console.log(testData);
        testData.push(d);
    });
    hot.render();
};

//default setting
var ssDefaultSettings = {
    // minRows: 200,
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
    search:true,
    sortIndicator: true,
    afterScrollVertically: function(e){
        compute_window(e);
        console.log("scroll down");
    }
    ,data: testData
}
// //initializing interface
var hot = new Handsontable(container, ssDefaultSettings);

// //search function
// Handsontable.dom.addEvent(searchFiled, 'keyup', function (event) {
//     var queryResult = hot.search.query(this.value);
//     hot.render();
// });
//
// // var baseUrl = 'http://localhost:3000/api/';
// var baseUrl = 'http://localhost:8080/';
//
// // get books
//
// var book;
// var sheet;
// var table;
// var r1, r2, c1, c2;
//
// //open book and import sheet data
// function openBook(){
//     console.log('openbook pressed');
//     book = $("#workbook").val();
//     console.log(book);
//
//     var sheetList = '';
//     $("#sheets").empty();
//
//     $.get(baseUrl + 'getSheets/'+ book, function(data) {
//         // var sheets = JSON.parse(data);
//         var sheets = data;
//         console.log(sheets);
//
//         $.each(sheets.sheets, function(index, value){
//             sheetList+='<button class="button sheetEntry is-small is-link">' + value + '</button>';
//         });
//         $("#sheets").append(sheetList);
//
//         $('.sheetEntry').click(function(){
//             sheet = $(this).text();
//             console.log("book:"+book+";sheet:"+sheet);
//             r1, r2, c1, c2 = 1, 200, 1, 24;
//             $.get(baseUrl + 'getCells/' + book + '/' + sheet + '/' + r1 + '-' + r2 + '/' + c1 + '-' +c2, function(data){
//                 console.log(data);
//                 var sheetData = JSON.parse(data);
//                 var displayData = []
//                 $.each(sheetData.cells, function(index, cell){
//                     displayData.push([parseInt(cell.row), parseInt(cell.col), cell.value]);
//                 });
//
//                 console.log(displayData);
//                 hot.setDataAtCell(displayData);
//                 // console.log(sheetData.cells);
//             })
//             // console.log($(this).text());24
//         });
//
//         console.log(sheets);
//     });
// }
//
// //click to open a sheet
// $('#openBook').click(function(event){
//     $.get(baseUrl + 'getBooks', function(data){
//         console.log(data);
//         var books = data;
//         // var books = JSON.parse(data);
//         var bookList = '';
//         $("#workbook").empty();
//         $.each(books.books,function(index, value) {
//             var entry = '<option>'+ value +'</option>'
//             bookList+=entry;
//         });
//
//         $("#workbook").append(bookList);
//         $("#workbook").val(books.books[1]);
//         console.log("in open book");
//
//         // $("#workbook").selectmenu("refresh");
//
//
//         var dialog = $('#openBookWindow').dialog({
//             buttons: {
//                 "Open": function() {
//                     openBook();
//                     dialog.dialog("close");
//                 },
//                 Cancel: function() {
//                     dialog.dialog("close");
//                 }
//             }
//         });
//     })
// });
//
//
// //
// // var fetchData = function(n) {
// //     return Handsontable.helper.createSpreadsheetData(n, 3);
// // };
// //
// // // load data and render
// // var loadMoreData = function(n) {
// //     // call data service
// //     var incoming = fetchData(n);
// //     incoming.forEach(function(d) {
// //         data.push(d);
// //     });
// //     hot.render();
// // };
// //
// // var compute_window = function(e) {
// //     var rowCount = hot.countRows();
// //     var rowOffset = hot.rowOffset();
// //     var visibleRows = hot.countVisibleRows();
// //     var lastRow = rowOffset + (visibleRows * 1);
// //     var lastVisibleRow = rowOffset + visibleRows + (visibleRows/2);
// //     var threshold = 15;
// //
// //     if(lastVisibleRow > (rowCount - threshold)) {
// //         loadMoreData(rowCount);
// //     }
// // };
//
//
// var navigationPanel = $('#navigationPanel');
//
// navigationPanel.click(function(){
//     console.log("navigational panel clicked");
//
//     if ($('#navChart').css('width')=='0px'){
//         $('#navChart').css('width', '200px');
//
//     } else {
//         $('#navChart').css('width', '0px');
//
//     }
// });
//
//
//
// // hot = new handsontable(,{
// //
// // })
//
//
// // document.addEventListener("DOMContentLoaded", function() {
// //     // mock service for getting data
// //     var fetchData = function(n) {
// //         return Handsontable.helper.createSpreadsheetData(n, 3);
// //     };
// //
// //     // variables
// //     var example = container;
// //     var hot;
// //
// //
// //
// //     var data = fetchData(100);
// //
// //     var compute_window = function(e) {
// //         var rowCount = hot.countRows();
// //         var rowOffset = hot.rowOffset();
// //         var visibleRows = hot.countVisibleRows();
// //         var lastRow = rowOffset + (visibleRows * 1);
// //         var lastVisibleRow = rowOffset + visibleRows + (visibleRows/2);
// //         var threshold = 15;
// //
// //         if(lastVisibleRow > (rowCount - threshold)) {
// //             loadMoreData(rowCount);
// //         }
// //     };
// //
// //     // initialize HOT
// //     hot = new Handsontable(example,{
// //         data: data,
// //         rowHeaders: true,
// //         colHeaders: true,
// //         afterScrollVertically: compute_window
// //     });
// //
// //     // load data and render
// //     var loadMoreData = function(n) {
// //         // call data service
// //         var incoming = fetchData(n);
// //         incoming.forEach(function(d) {
// //             data.push(d);
// //         });
// //         hot.render();
// //     };
// // })
//
//
//
// //
// // // button for editing schema of data
// // var editSchema = function(item){
// //     item.click(function(event){
// //         console.log("your are editing schema");
// //         alert("test message");
// //     })
// // };
// //
// // click to create a new table based on selected area
// var createTable = function(selectedRange){
//     console.log(selectedRange);
//     // [r,c,r2,c2] = selectedRange;
//     // console.log([r,c,r2,c2]);
//     // console.log(hot.getData(r,c,r2,c2));
//     var firstRowIsField = true;
//
//     for(var i=0;i<selectedRange.length;i++) {
//         [r,c,r2,c2] = selectedRange[i];
//
//         console.log([r,c,r2,c2]);
//
//         var dialog = $('#headerEditor').dialog({
//             buttons: {
//                 "Yes": function() {
//                     // openBook();
//                     console.log(hot);
//                     for (var j=c;j<=c2;j++){
//                         console.log([r,c,r2,c2]);
//                         hot.setCellMeta(r, j, 'type', 'dropdown' );
//                         hot.setCellMeta(r, j, 'source', ['ChangeType','ChangeData']);
//                         hot.setCellMeta(r, j, 'className', 'table-header');
//                         hot.setCellMeta(r, j, 'trimDropdown', 'false' );
//                     }
//
//                     for (var j=c;j<=c2;j++){
//                         for (var i=r+1;i<=r2;i++) {
//                             hot.setCellMeta(i, j, 'className', 'table-body');
//                             console.log([r,c,r2,c2]);
//                         }
//                     }
//                     hot.render();
//
//                     dialog.dialog("close");
//                 },
//                 Cancel: function() {
//                     dialog.dialog("close");
//                 }
//             }
//         });
//
//
//
//
//
//         // if (firstRowIsField === true){
//         //     console.log(hot.getData(r,c,r2,c2));
//
//         // } else {
//         // };
//
//     }
//
//
//     //pop up a window
//     // checkbox-use the first row as field name
//     // if unclick that checkbox, show a small panel
//     // for users to specify the field name. The selected range
//     // will be moved down a row.
// }
//
// //click to create table
// $('#createTable').click(function(event) {
//     var tableData = hot.getSelected();
//     console.log(tableData);
//     createTable(tableData);
//
//     hot.render();
//
// });
//
// //
// // // import a table and display it on a selected area
// // var importTable = function(row, col, data){
// //     var keys = data[0];
// //     var types = data[1];
// //     var values = data[2];
// //
// //     var c2 = col + keys.length;
// //     var r2 = row + values.length;
// //
// //     var header = [];
// //     var body = [];
// //     for(j=col; j<c2;j++ ){
// //         header.push([row, j, '<button>'+keys[j-col]+'</button>']);
// //         hot.setCellMeta(row, j, 'renderer', 'html' );
// //         // hot.setCellMeta(row, j, 'source', ['ChangeType','ChangeData']);
// //         hot.setCellMeta(row, j, 'className', 'table-header');
// //     };
// //
// //     hot.setDataAtCell(header);
// //
// //     $('.table-header').click(function(event){
// //        alert("Editing panel");
// //     });
// //
// //     for (j=col; j<c2;j++){
// //         for(i=row+1;i<=r2;i++){
// //             hot.setCellMeta(i,j,'className', 'table-body');
// //             body.push([i,j,values[i-row-1][j-col]]);
// //         }
// //     }
// //
// //     hot.setDataAtCell(body);
// // }
// //
// // // change property of data in selected area
// // var setPropAtRange = function(r,c,r2,c2, prop, value) {
// //     for (j=c;j<=c2;j++){
// //         for (i=r;i<=r2;i++) {
// //             hot.setCellMeta(i, j, prop, value);
// //
// //         }
// //     }
// //     hot.render();
// // }
// //
// // //click to trigger highlight function
// // $('#highlight').click(function(event){
// //     console.log("clicked highlight");
// //     [r,c,r2,c2] = hot.getSelected();
// //     console.log([r,c,r2,c2]);
// //
// //     setPropAtRange(r,c,r2,c2,'className','highlighted');
// // })
// //
// //
//
// //click to import a csv file for demo purpose
// $('#csvFile').change(function(event){
//     console.log("fire");
//     var file = event.target.files[0];
//     Papa.parse(file, {
//         complete: function(results) {
//             console.log("Finished:", results.data);
//             var data = results.data;
//
//             hot.loadData(results.data);
//             hot.updateSettings( ssDefaultSettings );
//
//         }
//     });
//
// });
//
// // // click to clear current interface
// // $("#newSheet").click(function(event) {
// //     hot.clear();
// // })
// //
// // // click to load data
// // $("#loadData").click(function(event) {
// //
// //     var data = JSON.parse('{"books":["GPA","strike_locations","global_inflation_rates","tuition_waivers"]}');
// //     var books = ""
// //     for (var i in data['books']) {
// //         books = books + '<option value="' + data['books'][i] + '">' + data['books'][i] + '</option>'
// //     }
// //
// //     $( ".modal-content" ).append( data );
// //
// //     $(".modal").addClass("is-active");
// //
// //     $(".modal-close").click(function() {
// //         $(".modal").removeClass("is-active");
// //     });
// //
// //     // $.get('http://localhost:3000/users', function(data) {
// //     //     var obj = jQuery.parseJSON(data);
// //     //     hot.loadData(obj);
// //     //     hot.updateSettings(ssDefaultSettings);
// //     // });
// //
// // });
// //
// //
//
// //
// // //click to import table
// // $('#importTable').click(function(event) {
// //     $.get('http://localhost:3000/tables', function(data) {
// //         var obj = JSON.parse(data);
// //
// //         [row, col, row2 , col2] = hot.getSelected();
// //         importTable(row,col,obj);
// //         hot.updateSettings(ssDefaultSettings);
// //     });
// // });
// //
// //
// //
// // ///////////////////////////////////////////////////
//
//
// // get sheets for a book
//
// // get book-sheet-row-col-value
