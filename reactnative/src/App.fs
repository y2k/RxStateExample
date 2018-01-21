namespace App

open Fable.Import.React
open Fable.PowerPack
module JS = Fable.Import.JS

type HomeComponent(props) as this =
    inherit Component<obj, Elm.Model>(props)

    do Elm.init ||> (fun model _ -> this.setInitState model)

    member this.componentDidMount() = 
        Elm.init ||> this.reloadState
            
    member this.render() : ReactElement = 
        Elm.view 
            this.state 
            (fun msg -> Elm.update this.state msg ||> this.reloadState)

    member this.reloadState state (cmd: JS.Promise<Elm.Msg> option) =
        this.setState state
        match cmd with
        | Some task -> 
            task 
            |> Promise.map (fun msg -> 
                let ns, newCmd = Elm.update this.state msg
                this.reloadState ns newCmd)
            |> Promise.start
        | None -> ()