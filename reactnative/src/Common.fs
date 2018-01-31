namespace Common

open System
open Fable.PowerPack

module Utils =
    let unit f a () = f a
    let flip f a b = f b a

module CommonUi =
    open Fable.Helpers.ReactNative
    open Fable.Helpers.ReactNative.Props

    let fullscreenIndicator =
        activityIndicator [ ViewProperties.Style [ Flex 1. ]
                            ActivityIndicator.Size Size.Large ]

    let space size = view [ ViewProperties.Style [ Height size ] ] []

    let buttonView title dispatch msg =
        touchableHighlight 
            [ TouchableHighlightProperties.Style [ Padding 12.; BackgroundColor "#f0f0f0" ] 
              TouchableHighlightProperties.ActiveOpacity 0.7
              TouchableHighlightProperties.UnderlayColor "#e0e0e0"
              OnPress (fun _ -> dispatch msg) ]
            [ text [] title ]

    let button title f disabled =
        touchableHighlight 
            [ TouchableHighlightProperties.Style [ Padding 12.; BackgroundColor "#f0f0f0" ] 
              TouchableHighlightProperties.ActiveOpacity 0.7
              TouchableHighlightProperties.UnderlayColor "#e0e0e0"
              OnPress <| if disabled then ignore else f ]
            [ text [ TextProperties.Style [ Color <| if disabled then "#d0d0d0" else "#010101" ] ] 
                   title ]

    let labeledTextInput props title (error: String option) =
        view [] 
             [ textInput props title
               error
               |> Option.map (fun _ -> text [ TextProperties.Style [ Color "red"; FontSize 20. ] ] "Error")
               |> Option.defaultValue (view [] []) ]

module Services =
    type SearchEngine = Google | Yandex
    let search (engine: SearchEngine) query =
        promise {
            let random = Random()
            do! Promise.sleep <| random.Next(1000, 2500)
            if random.NextDouble() < 0.3 then failwith "Network Error (test)"
            return
                random.Next(3, 32)
                |> List.unfold (fun x -> if x <= 0 then None else Some <| (sprintf "%O (%s) # %d" engine query (random.Next(10000)), x - 1))
        }

module Cmd =
    let ofPromise fOk fError p = p |> Promise.map fOk |> Promise.catch fError |> Some
