module Flow

open System
open Fable.Helpers.ReactNative
open Fable.Helpers.ReactNative.Props
open Common.CommonUi
open Common.Utils
module JS = Fable.Import.JS

type Stage = Home | Addresses | Billing 

type Model = 
    { stage: Stage
      isInputValid: Boolean
      input: String 
      itemNumbers: Int32
      shippingAddresses: String list
      billingAddress: String }
    static member Default = { stage = Home; isInputValid = false; input = ""; itemNumbers = 0; shippingAddresses = []; billingAddress = "" }

type Msg = 
    | InputChanged of input: String
    | ToNextStage of current: Stage

module Domain =
    open System.Text.RegularExpressions

    let private numberRegex = Regex("^\\d{1,2}$")
    let private notEmptyRegex = Regex("[^ ]+")

    let validate stage input =
        match stage with
        | Home -> numberRegex
        | Addresses -> notEmptyRegex
        | Billing -> notEmptyRegex
        |> fun regex -> regex.IsMatch input

    let makeBillingMessage model =
        match model.itemNumbers with
        | 0 -> "Buy something"
        | 1 -> sprintf "Item will be shipped to %s and billed to %s" model.shippingAddresses.[0] model.billingAddress
        | num ->
            let postfix = sprintf " accordingly. All %i items will be billed to %s." num model.billingAddress
            model.shippingAddresses 
            |> List.reduce (sprintf "%s, %s")
            |> (+) "Items will be shipped to "
            |> flip (+) postfix

let init: Model * JS.Promise<Msg> option = Model.Default, None

let update model msg: Model * JS.Promise<Msg> option = 
    match msg with
    | InputChanged input -> { model with input = input; isInputValid = Domain.validate model.stage input }, None
    | ToNextStage Home -> { Model.Default with stage = Addresses; itemNumbers = int model.input }, None
    | ToNextStage Addresses -> 
        { model with
                stage = if model.shippingAddresses.Length + 1 = model.itemNumbers then Billing else Addresses
                shippingAddresses = model.shippingAddresses @ [model.input]
                input = "" }, None
    | ToNextStage Billing -> { model with stage = Home; billingAddress = model.input; input = "" }, None

let viewHome model dispatch = 
    view [] 
         [ textInput 
               [ TextInput.OnChangeText (InputChanged >> dispatch) 
                 TextInput.KeyboardType KeyboardType.Numeric
                 TextInput.Placeholder "How many items are you going go buy?" ] 
               model.input
           button "Begin" (unit ToNextStage Home >> dispatch) (not <| Domain.validate model.stage model.input)
           text [] <| Domain.makeBillingMessage model ]

let viewAddresses model dispatch = 
    view [] 
         [ textInput 
               [ TextInput.OnChangeText (InputChanged >> dispatch) 
                 TextInput.Placeholder <| sprintf "Shipping address for item #%i" (model.shippingAddresses.Length + 1) ] 
               model.input
           button "Next" (unit ToNextStage Addresses >> dispatch) (not <| Domain.validate model.stage model.input) ]

let viewBilling model dispatch = 
    view [] 
         [ textInput 
               [ TextInput.OnChangeText (InputChanged >> dispatch) 
                 TextInput.Placeholder "Billing address" ] 
               model.input
           button "Next" (unit ToNextStage Billing >> dispatch) (not <| Domain.validate model.stage model.input) ]

let view model dispatch =
    match model.stage with
        | Home -> viewHome model dispatch
        | Addresses -> viewAddresses model dispatch
        | Billing -> viewBilling model dispatch
    |> List.singleton
    |> view [ ViewProperties.Style [ JustifyContent JustifyContent.Center; Padding 8.; Flex 1. ] ]