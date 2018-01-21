module Elm

open System
open Fable.Helpers.ReactNative
open Fable.Helpers.ReactNative.Props
open Common
open Common.CommonUi
module JS = Fable.Import.JS 

type Model = 
    { query: String
      isFlight: Boolean
      searchResults: String list
      error: Exception option }

type Msg = 
| QueryChanged of String
| SearchRequest of Services.SearchEngine
| SearchSuccess of String list
| SearchFailed of Exception

let init: Model * JS.Promise<Msg> option = 
    { query = ""; isFlight = false; searchResults = []; error = None }, None

let update (model: Model) = function
    | QueryChanged query -> { model with query = query }, None
    | SearchRequest engine -> 
        { model with isFlight = true }, 
        Services.search engine model.query |> Cmd.ofPromise SearchSuccess SearchFailed
    | SearchSuccess result -> { model with isFlight = false; searchResults = result; error = None }, None
    | SearchFailed e -> { model with isFlight = false; error = Some e }, None

let private contentView model dispatch =
    view [ ViewProperties.Style [ PaddingTop 35.; Padding 8. ] ]
         [ labeledTextInput
               [ TextInput.OnChangeText (QueryChanged >> dispatch) ]
               model.query
               (model.error |> Option.map string)

           buttonView "Search in Google" dispatch (SearchRequest Services.Google)
           space 4.
           buttonView "Search in Yandex" dispatch (SearchRequest Services.Yandex)

           text [] (List.fold (sprintf "%s\n%s") "" <| model.searchResults) ]

let view model dispatch = 
    if model.isFlight then fullscreenIndicator 
    else contentView model dispatch