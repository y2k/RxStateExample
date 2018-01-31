namespace App

open Fable.Import.React
open Fable.PowerPack
module JS = Fable.Import.JS

module Example = Flow

type HomeComponent(props) as this =
    inherit Component<obj, Example.Model>(props)

    do Example.init ||> (fun model _ -> this.setInitState model)

    member this.componentDidMount() = 
        Example.init ||> this.reloadState
            
    member this.render() : ReactElement = 
        Example.view 
            this.state 
            (fun msg -> Example.update this.state msg ||> this.reloadState)

    member this.reloadState state (cmd: JS.Promise<Example.Msg> option) =
        this.setState state
        match cmd with
        | Some task -> 
            task 
            |> Promise.map (fun msg -> 
                let ns, newCmd = Example.update this.state msg
                this.reloadState ns newCmd)
            |> Promise.start
        | None -> ()